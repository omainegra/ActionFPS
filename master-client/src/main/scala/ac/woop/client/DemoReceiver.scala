package ac.woop.client

import akka.util.ByteString
import io.enet.akka.ENetService._
import io.enet.akka.Shapper.SendMessageAddition

case class DemoPacket(millis: Int, chan: Int, data: ByteString)
case class DemoReceiverParser(remote: PeerId) {
  def startMessage = SendMessageAddition(remote, 0)(22, ByteString.empty)
  def endMessage = SendMessageAddition(remote, 0)(23, ByteString.empty)
  def unapply(a: Any): Option[DemoPacket] = {
    PartialFunction.condOpt(a) {
      case pp @ PacketFromPeer(`remote`, 3, data) =>
        val (header, rest) = data.splitAt(12)
        // todo not sure what len is for. Investigate!
        val (millis, chan, len) = {
          val buffer = header.asByteBuffer
          (java.lang.Integer.reverseBytes(buffer.getInt),
            java.lang.Integer.reverseBytes(buffer.getInt),
            java.lang.Integer.reverseBytes(buffer.getInt))
        }
        DemoPacket(millis = millis, chan = chan, data = rest)
    }
  }
}