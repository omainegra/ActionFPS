package actionfps.master.client.logstream

import akka.util.ByteString
import io.enet.akka.ENetService.{PacketFromPeer, PeerId}
import io.enet.akka.Shapper.SendMessageAddition
import io.enet.akka.Compressor._

case class LogReceiverParser(remote: PeerId) {
  def startMessage = SendMessageAddition(remote, 0)(20, ByteString.empty)

  def stopMessage = SendMessageAddition(remote, 0)(21, ByteString.empty)

  object #:~: {
    def unapply(input: ByteString): Option[(Int, ByteString)] = {
      if (input.size < 4)
        None
      else {
        val (intBytes, rest) = input.splitAt(4)
        val buf = intBytes.asByteBuffer
        Option((java.lang.Integer.reverseBytes(buf.getInt), rest))
      }
    }
  }

  def unapply(a: Any): Option[LogMessageReceived] = {
    PartialFunction.condOpt(a) {
      case pp@PacketFromPeer(`remote`, 1, id #::: millis #:~: level #:: message ##:: ByteString.empty) =>
        LogMessageReceived(
          id = id,
          millis = millis,
          level = level,
          message = message
        )
    }
  }
}
