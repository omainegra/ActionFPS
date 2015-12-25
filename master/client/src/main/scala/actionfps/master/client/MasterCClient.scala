package actionfps
package master
package client

import actionfps.master.client.authentication.AuthenticatorTrait
import actionfps.master.client.demo.DemoReceiverParser
import actionfps.master.client.logstream.LogReceiverParser
import actionfps.master.client.sendkeys.{KeyExchanger, ServerUser}
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef, Props}
import io.enet.akka.ENetService
import io.enet.akka.ENetService._

class MasterCClient(enetService: ActorRef => Props,
                    val remote: PeerId,
                    val serverKey: String)
  extends Act
  with ActorLogging
  with AuthenticatorTrait {

  val service = context.actorOf(enetService(self))
  val logReceiver = LogReceiverParser(remote)
  val demoReceiver = DemoReceiverParser(remote)

  whenStarting {
    service ! ConnectPeer(remote, 4)
  }

  def exchangeKeys(keyExchanger: KeyExchanger): Unit = {
    log.info("Beginning key exchange")
    service ! keyExchanger.beginSending
    keyExchanger.pushOutMessages.foreach(service ! _)
    service ! keyExchanger.finishSending
    log.info("Completing key exchange")
  }

  become {
    case conn: ConnectedPeer =>
      beginAuthentication {
        context.parent ! Authenticated
        service ! logReceiver.startMessage
        service ! demoReceiver.startMessage
        become {
          case logReceiver(logMessage) =>
            log.info("Received log message {}", logMessage)
          case demoReceiver(demoMessage) =>
            log.info("Received demo message {}", demoMessage)
          case sendkeys.SendKeys(serverUsers) =>
            exchangeKeys(KeyExchanger(
              remote = remote,
              users = serverUsers
            ))
          case KeyExchanger.Completed(_, message) =>
            log.info(message)
        }
      }
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case p: PacketFromPeer =>
        log.info("Unexpected message received: {}", p)
        context stop self
      case f: FailedAuthentication =>
        log.info("Failed to authenticate: {}", f)
        context stop self
      case dp@DisconnectedPeer(`remote`) =>
        log.info("Unexpected disconnection: {}", dp)
        context stop self
      case _ =>
        super.unhandled(message)
    }
  }

}

object MasterCClient {

  case class MyProps(remote: PeerId, serverKey: String) {
    def defaultEnetService(to: ActorRef) = {
      val enetParams = ENetServiceParameters(
        listen = None,
        maxConnections = 1,
        channelCount = 4
      )
      Props(new ENetService(to, enetParams))
    }

    def apply(enetService: ActorRef => Props): Props = Props(new MasterCClient(
      enetService = enetService, remote = remote, serverKey = serverKey
    ))

    def apply(): Props = apply(defaultEnetService)
  }

}
