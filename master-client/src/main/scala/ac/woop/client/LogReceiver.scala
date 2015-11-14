package ac.woop.client

import ac.woop.client.MasterClient.Repository.Server
import akka.actor.{Terminated, ActorRef, ActorLogging}
import akka.util.ByteString
import io.enet.akka.{Shapper, Compressor}
import io.enet.akka.ENetService._
import Compressor._
import io.enet.akka.Shapper.packetFromPeerExtra
import akka.actor.ActorDSL._
import Shapper._

class LogReceiver(service: ActorRef, remote: PeerId, server: Server) extends Act with ActorLogging {
  whenStarting {
    service ! SendMessageAddition(remote, 0)(20, ByteString.empty)
  }
  whenStopping {
    service ! SendMessageAddition(remote, 0)(21, ByteString.empty)
  }
  object #:~: {
    def unapply(input: ByteString): Option[(Int, ByteString)] = {
      if ( input.size < 4 )
        None
      else {
        val (intBytes, rest) = input.splitAt(4)
        val buf = intBytes.asByteBuffer
        Option((java.lang.Integer.reverseBytes(buf.getInt), rest))
      }
    }
  }
  become {
    case pp @ PacketFromPeer(`remote`, 1, id #::: millis #:~: level #:: message ##:: ByteString.empty) =>
      log.info("Received message in log channel: {}, {}, {}, {}", id, millis, level, message)
  }
}