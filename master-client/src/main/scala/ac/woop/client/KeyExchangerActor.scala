package ac.woop.client

import ac.woop.client.model.Servers
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef}
import akka.util.ByteString
import io.enet.akka.Compressor
import io.enet.akka.Compressor._
import io.enet.akka.ENetService._
import io.enet.akka.Shapper.SendMessageAddition

case class ServerUser(userId: String, sharedKey: String, userData: String)

object KeyExchanger {

  case object ExchangeComplete

}

case class KeyExchanger(remote: PeerId, users: List[ServerUser]) {

  def sendingLog = s"Sending ${users.size} users to $remote"

  def beginSending = SendMessageAddition(remote, 0)(10, users.size)

  def finishSending = SendMessageAddition(remote, 0)(12, ByteString.empty)

  def pushOutMessages: List[SendMessage] = {
    for {
      groupings <- users.grouped(10)
      bitums = for {
        ServerUser(userId, sharedKey, userData) <- groupings
        userPacketByteString = Compressor.stringToByteString(userId) ++ Compressor.stringToByteString(sharedKey) ++ Compressor.stringToByteString(userData)
      } yield userPacketByteString
    } yield SendMessageAddition(remote, 0)(bitums.foldLeft(ByteString(11.toByte))(_ ++ _), ByteString.empty)
  }.toList

  object Completed {
    def unapply(a: Any): Option[(KeyExchanger.ExchangeComplete.type, String)] = {
      PartialFunction.condOpt(a) {
        case pp4@PacketFromPeer(`remote`, 0, 13 #:: ByteString.empty) =>
          (KeyExchanger.ExchangeComplete, "Ack received from server. Key Exchange complete.")
      }
    }
  }

}

class KeyExchangerActor(service: ActorRef, remote: PeerId, server: Servers, users: List[ServerUser]) extends Act with ActorLogging {
  val kx = KeyExchanger(remote, users)
  whenStarting {
    log.info(kx.sendingLog)
    service ! kx.beginSending
    kx.pushOutMessages.foreach(m => service ! m)
    service ! kx.finishSending
  }
  become {
    case kx.Completed(ex, message) =>
      log.info(message)
      context.parent ! ex
      context stop self
  }
}
