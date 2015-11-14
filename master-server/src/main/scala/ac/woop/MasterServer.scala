package ac.woop

import java.security.{SecureRandom, Security, MessageDigest}

import ac.woop.MasterServer.Repository
import ac.woop.MasterServer.Repository._
import ac.woop.client.Authenticator.Authenticated
import ac.woop.client.KeyExchanger.ExchangeComplete
import ac.woop.client.MasterCClient
import akka.actor._
import akka.event.LoggingReceive
import akka.util.ByteString
import io.enet.akka.{Compressor, ENetService}
import io.enet.akka.ENetService._
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import akka.actor.ActorDSL._
import Compressor._
import io.enet.akka.Shapper.packetFromPeerExtra
import org.h2.mvstore.MVStore
import scala.annotation.tailrec

object MasterClientCentral {
  sealed trait MasterCommand
  case class CreateNewUserKey(userId: UserId) extends MasterCommand
  case class CreateNewServerKey(serverId: ServerId) extends MasterCommand
  case class PutUser(userId: UserId, userData: UserData) extends MasterCommand
  case class PutServer(serverId: ServerId) extends MasterCommand
  case class DeleteUser(userId: UserId) extends MasterCommand
  case class DeleteServer(serverId: ServerId) extends MasterCommand

  case class PushUsersToServer(toServer: ServerId) extends MasterCommand
  case object PushUsers extends MasterCommand

  sealed trait MasterRequest
  case object GiveRepository extends MasterRequest

  sealed trait MasterEvent
  case class UserUpdated(userId: UserId, userData: UserData, userKey: Key) extends MasterEvent
  case class UserDeleted(userId: UserId) extends MasterEvent
  case class ServerUpdated(serverId: ServerId, serverKey: Key) extends MasterEvent
  case class ServerDeleted(serverId: ServerId) extends MasterEvent

}
class PersistentRepository(database: MVStore) {
  val userKeysDatabase = database.openMap[String, String]("user-keys")
  val userDataDatabase = database.openMap[String, String]("user-data")
  val serverKeysDatabase = database.openMap[String, String]("server-keys")
  def loadRepository: Repository = {
    import collection.JavaConverters._
    val serverIdParser = s"""(.*):(\\d+)""".r
    val ukS = userKeysDatabase.asScala
    val udS = userDataDatabase.asScala
    Repository(
      servers =
        (for { (serverIdParser(serverHost, serverPort), serverKey) <- serverKeysDatabase.asScala }
        yield ServerId(serverHost, serverPort.toInt) -> Server(serverKey)).toMap,
      users =
        (
          for {
            (userIdStr, userKeyStr) <- ukS
            userDataStr <- udS.get(userIdStr)
          } yield UserId(userIdStr) -> User(userKeyStr, UserData(userDataStr))
          ).toMap
    )
  }
}
class MasterClientCentral(dbFile: String) extends Act with ActorLogging {
  import MasterClientCentral._
//  var repository = Repository.empty
  val database = MVStore.open(dbFile)
  val persistentRepository = new PersistentRepository(database)
  import persistentRepository._

  whenStarting {
    log.info("Initialising master cont")
    become(withRepository(loadRepository))
    self ! PushUsers
  }
  // could optimise - repository.with*** could potentially be slow-ish methods.
  // And we don't want too much blocking inside our actors.
  def withRepository(repository: Repository): Receive = {
    case CreateNewUserKey(userId @ UserId(userIdStr)) =>
      val userData = repository.users.get(userId).map(_.data).getOrElse(UserData.empty)
      val newKey = Repository.randomKey
      become(withRepository(repository.withNewUser(userId, userData, newKey)))
      userKeysDatabase.put(userIdStr, newKey)
      database.commit()
      val response = UserUpdated(userId, userData, newKey)
      sender() ! response
      context.system.eventStream.publish(response)
      self ! PushUsers
    case PutUser(userId @ UserId(userIdStr), userData @ UserData(userDataStr)) =>
      val newKey = repository.users.get(userId).map(_.key).getOrElse(Repository.randomKey)
      userKeysDatabase.put(userIdStr, newKey)
      userDataDatabase.put(userIdStr, userDataStr)
      database.commit()
      become(withRepository(repository.withNewUser(userId, userData, newKey)))
      val response = UserUpdated(userId, userData, newKey)
      context.system.eventStream.publish(response)
      self ! PushUsers
      sender() ! response
    case PutServer(serverId: ServerId) =>
      val newKey = repository.servers.get(serverId).map(_.key).getOrElse(Repository.randomKey)
      serverKeysDatabase.put(serverId.asString, newKey)
      database.commit()
      become(withRepository(repository.withNewServer(serverId, newKey)))
      val response = ServerUpdated(serverId, newKey)
      context.system.eventStream.publish(response)
      sender() ! response
      self ! PushUsersToServer(serverId)
    case CreateNewServerKey(serverId) =>
      val newKey = Repository.randomKey
      serverKeysDatabase.put(serverId.asString, newKey)
      database.commit()
      become(withRepository(repository.withNewServer(serverId, newKey)))
      val response = ServerUpdated(serverId, newKey)
      context.system.eventStream.publish(response)
      sender() ! response
      self ! PushUsersToServer(serverId)
    case DeleteUser(userId @ UserId(userIdStr)) =>
      userKeysDatabase.remove(userIdStr)
      userDataDatabase.remove(userIdStr)
      database.commit()
      become(withRepository(repository.withoutUser(userId)))
      context.system.eventStream.publish(UserDeleted(userId))
      self ! PushUsers
    case DeleteServer(serverId) =>
      serverKeysDatabase.remove(serverId.asString)
      database.commit()
      become(withRepository(repository.withoutServer(serverId)))
      context.system.eventStream.publish(ServerDeleted(serverId))
    case GiveRepository =>
      sender() ! repository
    case PushUsers =>
      for { serverId <- repository.servers.keysIterator } self ! PushUsersToServer(serverId)
    case PushUsersToServer(toServer) =>
      log.info("Receiving push command for server {} - current repository {}", toServer, repository)
      // simply let it do its thing, we don't care about the result for now actually
      // Launch & forget. It'll fail if it takes too long.
      actor(context)(new MasterCClient(toServer.asPeerId, repository.serverUsers(toServer), repository.servers(toServer)))
    case ExchangeComplete =>
      log.info("Client had completed exchange.")
      context.stop(sender())
    case DisconnectedPeer(peerId) =>
      implicit class addServerId(peerId: PeerId) {
        def serverId = ServerId(peerId.host, peerId.port)
      }
      import context.dispatcher
      import concurrent.duration._
      context.system.scheduler.scheduleOnce(10.seconds, self, PushUsersToServer(peerId.serverId))
  }
}

object MasterServerApp extends App {
  implicit val as = ActorSystem("whut")
  val aaa = actor(new Act {
    whenStarting {
      implicit class addServerId(peerId: PeerId) {
        def serverId = ServerId(peerId.host, peerId.port)
      }
      val mainServerPeerId = PeerId("127.0.0.1", 123)
      val mainServerId = mainServerPeerId.serverId
      val key = Repository.randomKey
      val users =
        for {
          i <- 1 to 100000
          userId = s"User$i"
        } yield UserId(userId) -> User(key, UserData("test-data"))
      val woopServerPeer = PeerId("aura.woop.ac",8999)
      val woopServerId = woopServerPeer.serverId
      val nrepo = Repository(servers = Map.empty, users = users.toMap)
        .withNewServer(mainServerId, "wat")
        .withNewServer(woopServerId, "mysuperkey")
//      val repo = Repository.empty.withNewServer(mainServerId, "wat").withNewUser(UserId("drakas"), UserData("Hello"))
//      val nrepo = (1 to 5000).foldLeft(repo){case (r, no) => r.withNewUser(UserId(s"User$no."), UserData("Test"))}
//      Respository(users = copy(users = users.updated(userId, User(Repository.randomKey, data)))
//      val acS = actor(context, name="AcServer")(new AcServer(mainServerPeerId, "wat"))
//      val chld = actor(context, name="MasterClient")(new MasterCClient(mainServerPeerId, nrepo.serverUsers(mainServerId), nrepo.servers(mainServerId)))
//      val chld = actor(context, name="MasterClient")(new MasterCClient(woopServerPeer, nrepo.serverUsers(woopServerId), nrepo.servers(woopServerId)))
//      import context.dispatcher
//      import concurrent.duration._
//      context.system.scheduler.scheduleOnce(5.seconds){
//        val chld2 = actor(context, name="MasterClient2")(new MasterCClient(mainServerPeerId, repo, ServerId("woop.ac:1999")))
//      }
    }
  })
}
object MasterServer {
  // http://stackoverflow.com/a/2208446
  /** Connect to the server, server sends X, we send H(X), then we send Y, they send H(Y), then connection is established **/
  /** Then we ask server 'list users pls', take that list, send him any updates. **/
  object Repository {

    type Users = Map[UserId, User]
    type Servers = Map[ServerId, Server]
    type Key = String

    case class UserData(data: String)
    object UserData {
      def empty = UserData("")
    }
    case class UserId(userId: String)
    case class ServerId(hostname: String, port: Int) {
      def asString = s"$hostname:$port"
      def asPeerId = PeerId(hostname, port)
    }
    case class User(key: Key, data: UserData)
    case class Server(key: Key)
    case class UserServer(key: Key, data: UserData)

    type UserServers = Map[(UserId, ServerId), UserServer]
    Security.addProvider(new BouncyCastleProvider())
    val digester = MessageDigest.getInstance("SHA-256", "BC")

    def randomKey = {
      val random = new SecureRandom()
      val arr = Array.fill(20)(1.toByte)
      random.nextBytes(arr)
      val key = digester.digest(arr)
      Hex.encodeHexString(key)
    }
    def empty = Repository(Map.empty, Map.empty)
  }
  case class Repository(users: Users, servers: Servers) {
    val serverUsers = for {
      (serverId, _) <- servers
    } yield serverId -> (for {
      (userId, User(userKey, data)) <- users
      userServerKey = {
        val inputString = userKey + serverId.asString
        val sharedKey = Repository.digester.digest(inputString.getBytes)
        Hex.encodeHexString(sharedKey)
      }
    } yield userId -> UserServer(userServerKey, data))
    def withNewUser(userId: UserId, data: UserData) =
      copy(users = users.updated(userId, User(Repository.randomKey, data)))
    def withNewUser(userId: UserId, data: UserData, key: Key) =
      copy(users = users.updated(userId, User(key, data)))
    def withNewServer(serverId: ServerId) =
      copy(servers = servers.updated(serverId, Server(Repository.randomKey)))
    def withNewServer(serverId: ServerId, key: String) =
      copy(servers = servers.updated(serverId, Server(key)))
    def withoutUser(userId: UserId) =
      copy(users = users - userId)
    def withoutServer(serverId: ServerId) =
      copy(servers = servers - serverId)
    override def toString = s"""Repository($users, $servers, $serverUsers)"""
  }

}