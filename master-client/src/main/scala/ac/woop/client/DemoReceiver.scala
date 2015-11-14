package ac.woop.client

import ac.woop.MasterServer.Repository._
import ac.woop.demo.DemoParser
import akka.actor.{Terminated, ActorRef, ActorLogging}
import akka.util.ByteString
import io.enet.akka.ENetService._
import akka.actor.ActorDSL._
class DemoReceiver(service: ActorRef, remote: PeerId, server: Server) extends Act with ActorLogging {
  whenStarting {
    service ! SendMessage(remote, 0)(22, ByteString.empty)
  }
  whenStopping {
    service ! SendMessage(remote, 0)(23, ByteString.empty)
  }
  become {
    case pp @ PacketFromPeer(`remote`, 3, data) =>
      val (header, rest) = data.splitAt(12)
      val (millis, chan, len) = {
        val buffer = header.asByteBuffer
        (java.lang.Integer.reverseBytes(buffer.getInt),
          java.lang.Integer.reverseBytes(buffer.getInt),
          java.lang.Integer.reverseBytes(buffer.getInt))
      }
//      log.info("Received message in demo channel: millis = {}, chan = {}, len = {}, data = {}", millis, chan, len, rest)
      for { (p, other) <- DemoParser.parsePosition(rest) }
        log.info("Received position, {} from {}", p, rest)
  }
}
object DemoReceiver {
  case class DemoPacket(millis: Int, chan: Int, data: ByteString)
}