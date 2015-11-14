package ac.woop.client

import ac.woop.client.KeyExchanger.ExchangeComplete
import ac.woop.client.MasterClient.Repository
import ac.woop.client.MasterClient.Repository._
import akka.actor.ActorDSL._
import akka.actor._
import io.enet.akka.ENetService._
import org.h2.mvstore.MVStore

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
