package ac.woop.client

import akka.actor.ActorDSL._
import akka.actor.{Props, ActorRef, ActorLogging}
import io.enet.akka.ENetService
import io.enet.akka.ENetService._
object MasterCClient {
  case class MyProps(remote: PeerId, serverKey: String, exchangeKeysImmediately: Option[List[ServerUser]]) {
    def defaultEnetService(to: ActorRef) = {
      val enetParams = ENetServiceParameters(
        listen = None,
        maxConnections = 1,
        channelCount = 4
      )
      Props(new ENetService(to, enetParams))
    }
    def apply(enetService: ActorRef => Props):Props = Props(new MasterCClient(
        enetService = enetService, remote = remote, serverKey = serverKey, exchangeKeysImmediately = exchangeKeysImmediately
      ))
    def apply(): Props = apply(defaultEnetService)
  }
}
class MasterCClient(enetService: ActorRef => Props, val remote: PeerId, val serverKey: String, exchangeKeysImmediately: Option[List[ServerUser]]) extends Act with ActorLogging with AuthenticatorTrait {

  val logReceiver = LogReceiverParser(remote)
  val demoReceiver = DemoReceiverParser(remote)

  val service = context.actorOf(enetService(self))

  service ! ConnectPeer(remote, 4)

  var keyExchanger = KeyExchanger(remote, exchangeKeysImmediately.toList.flatten)

  def exchangeKeys(): Unit = {
    service ! keyExchanger.beginSending
    keyExchanger.pushOutMessages.foreach(service ! _)
    service ! keyExchanger.finishSending
  }

  become {
    case conn: ConnectedPeer =>
      beginAuthentication {
        if ( exchangeKeysImmediately.nonEmpty ) {
          exchangeKeys()
        }
        service ! logReceiver.startMessage
        service ! demoReceiver.startMessage
        become {
          case logReceiver(logMessage) =>
            log.info("Received log message {}", logMessage)
          case demoReceiver(demoMessage) =>
            log.info("Received demo message {}", demoMessage)
          case m if keyExchanger.Completed.unapply(m).isDefined =>
            keyExchanger.Completed.unapply(m).foreach{ case (com, msg) =>
              log.info(msg)
            }
        }
      }
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case p: PacketFromPeer =>
        log.info("Unexpected message received: {}", p)
        context stop self
      case dp @ DisconnectedPeer(`remote`) =>
        log.info("Unexpected disconnection: {}", dp)
        context stop self
      case _ =>
        super.unhandled(message)
    }
  }

}
