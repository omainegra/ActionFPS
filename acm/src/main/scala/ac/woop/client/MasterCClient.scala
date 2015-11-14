package ac.woop.client

import ac.woop.MasterServer.Repository
import ac.woop.MasterServer.Repository._
import ac.woop.client.Authenticator.{AuthenticationFailed, Authenticated}
import ac.woop.client.KeyExchanger.ExchangeComplete
import ac.woop.client.MasterCClient.PushServerUsers
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef, Terminated}
import io.enet.akka.ENetService
import io.enet.akka.ENetService._
object MasterCClient {
  case class PushServerUsers(serverUsers: Map[UserId, UserServer])
}
class MasterCClient(remote: PeerId, initialServerUsers: Map[UserId, UserServer], server: Server) extends Act with ActorLogging {

  implicit class addServerId(peerId: PeerId) {
    def serverId = ServerId(peerId.host, peerId.port)
  }

  def this(serverId: ServerId, repository: Repository) = {
    this(serverId.asPeerId, repository.serverUsers(serverId), repository.servers(serverId))
  }

  val service = actor(context)(new ENetService(self, ENetServiceParameters(listen = None, maxConnections = 1, channelCount = 4)))

  def authenticating(authenticator: ActorRef): Receive = {
    case packet: PacketFromPeer =>
      authenticator ! packet
    case d: DisconnectedPeer =>
      authenticator ! d
      context.parent ! d
      context stop self
    case Authenticated =>
      log.info("Authenticated successfully, moving on...")
//      logReceiver = Option(actor(context, name = "log-receiver")(new LogReceiver(service, remote, server)))
//      demoReceiver = Option(actor(context, name = "demo-receiver")(new DemoReceiver(service, remote, server)))
      self ! PushServerUsers(initialServerUsers)
      context.parent ! Authenticated
      become(authenticated)
    case AuthenticationFailed =>
      throw new RuntimeException("Authentication failed. Possibly the wrong packet.")
    case CheckAuthenticateTimeout =>
      throw new RuntimeException("Authentication timed out.")
  }

  var demoReceiver = Option.empty[ActorRef]
  var logReceiver = Option.empty[ActorRef]
  var keyExchanger = Option.empty[ActorRef]

  def authenticated: Receive = {
    case packet: PacketFromPeer =>
      logReceiver.foreach(_ ! packet)
      demoReceiver.foreach(_ ! packet)
      keyExchanger.foreach(_ ! packet)
    case PushServerUsers(serverUsers) =>
      // todo ensure key exchange finishes before we give it another one
      keyExchanger = Option(actor(context, name="key-exchanger")(new KeyExchanger(service, remote, server, serverUsers)))
      context.watch(keyExchanger.get)
    case d: DisconnectedPeer =>
      context.parent ! d
      context stop self
    case Terminated(keyer) if keyExchanger.contains(keyer) =>
      keyExchanger = Option.empty
    case ExchangeComplete =>
      context.parent ! ExchangeComplete
  }

  def connecting: Receive = {
    case conn: ConnectedPeer =>
      val authenticator = actor(context, name = "authenticator")(new Authenticator(service, remote, server))
      authenticator ! conn
      become(authenticating(authenticator))
      import context.dispatcher
      import scala.concurrent.duration._
      context.system.scheduler.scheduleOnce(5.seconds, self, CheckAuthenticateTimeout)
    case d: DisconnectedPeer =>
      context.parent ! d
      context stop self
    case CheckConnectTimeout =>
      throw new RuntimeException(s"Failed to connect to remote $remote")
  }

  case object CheckAuthenticateTimeout
  case object CheckConnectTimeout
  whenStarting {
    service ! ConnectPeer(remote, 4)
    become(connecting)
    import context.dispatcher
    import scala.concurrent.duration._
    context.system.scheduler.scheduleOnce(5.seconds, self, CheckConnectTimeout)
  }

}
