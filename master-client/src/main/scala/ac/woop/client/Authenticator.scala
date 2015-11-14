package ac.woop.client

import ac.woop.client.Authenticator.{AuthenticationFailed, Authenticated}
import ac.woop.MasterServer.Repository
import ac.woop.MasterServer.Repository._
import akka.actor.{Terminated, ActorRef, ActorLogging}
import akka.util.ByteString
import io.enet.akka.Compressor
import io.enet.akka.ENetService._
import org.apache.commons.codec.binary.Hex
import Compressor._
import io.enet.akka.Shapper.packetFromPeerExtra
import akka.actor.ActorDSL._

object Authenticator {
  case object Authenticated
  case class AuthenticationFailed(reason: String)
}
class Authenticator(service: ActorRef, remote: PeerId, server: Server) extends Act with ActorLogging {
  become {
    case ConnectedPeer(`remote`) =>
      log.info("Connected to peer {}", remote)
      val challengeToServer = Hex.encodeHexString(scala.util.Random.nextString(32).toArray.map(_.toByte))
      val expectedResponse = Hex.encodeHexString(Repository.digester.digest((server.key + challengeToServer).getBytes))
      log.info("Challenge {} sent, expected response {}", challengeToServer, expectedResponse)
      service ! SendMessage(remote, 0)(0, challengeToServer)
      become {
        case pp@PacketFromPeer(`remote`, 0, 1 #:: `expectedResponse` ##:: ByteString.empty) =>
          log.info("Server identified himself correctly.")
          service ! pp.reply(2)
          become {
            case pp2@PacketFromPeer(`remote`, 0, 3 #:: challenge ##:: ByteString.empty) =>
              val myResponse = Hex.encodeHexString(Repository.digester.digest((server.key + challenge).getBytes))
              log.info("Server sent challenge {}, responding with {}", challenge, myResponse)
              service ! pp2.reply(4, myResponse)
              become {
                case pp3@PacketFromPeer(`remote`, 0, 5 #:: ByteString.empty) =>
                  log.info("Authentication was successful.")
                  context.parent ! Authenticated
                  context stop self
              }
          }
        case pp@PacketFromPeer(`remote`, 0, 1 #:: gotResponse ##:: ByteString.empty) =>
          log.info("Wrong response received. Expected challenge response {}, got response {}", expectedResponse, gotResponse)
          context.parent ! AuthenticationFailed(s"Wrong response received. Expected challenge response $expectedResponse, got response $gotResponse")
          context stop self
        case p: PacketFromPeer if p.channelID == 0  =>
          log.info("Unexpected message received. Expected challenge response {}, got message {}", expectedResponse, p)
          context.parent ! AuthenticationFailed
          context stop self
      }
  }
}