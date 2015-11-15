package actionfps
package master
package client
package sendkeys

import akka.util.ByteString
import io.enet.akka.Compressor
import io.enet.akka.Compressor._
import io.enet.akka.ENetService.{PacketFromPeer, PeerId, SendMessage}
import io.enet.akka.Shapper.SendMessageAddition

case class KeyExchanger(remote: PeerId, users: List[ServerUser]) {

  def sendingLog = s"Sending ${users.size} users to $remote"

  def beginSending = SendMessageAddition(remote, 0)(aboutToSendKeys, users.size)

  def finishSending = SendMessageAddition(remote, 0)(finishSendingKeys, ByteString.empty)

  def pushOutMessages: List[SendMessage] = {
    for {
      groupings <- users.grouped(10)
      bitums = for {
        ServerUser(userId, sharedKey, userData) <- groupings
        userPacketByteString = Compressor.stringToByteString(userId) ++ Compressor.stringToByteString(sharedKey) ++ Compressor.stringToByteString(userData)
      } yield userPacketByteString
    } yield SendMessageAddition(remote, 0)(bitums.foldLeft(ByteString(heresSomeKeys.toByte))(_ ++ _), ByteString.empty)
  }.toList

}
object KeyExchanger {

  object Completed {
    def unapply(a: Any): Option[(ExchangeComplete.type, String)] = {
      PartialFunction.condOpt(a) {
        case pp4@PacketFromPeer(_, 0, `serverReceivedKeys` #:: ByteString.empty) =>
          (ExchangeComplete, "Ack received from server. Key Exchange complete.")
      }
    }
  }

}