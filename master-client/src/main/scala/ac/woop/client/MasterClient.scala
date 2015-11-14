package ac.woop.client

import java.security.{SecureRandom, MessageDigest, Security}

import ac.woop.client.MasterClient.Repository._
import io.enet.akka.ENetService.PeerId
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.jce.provider.BouncyCastleProvider

object MasterClient {
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