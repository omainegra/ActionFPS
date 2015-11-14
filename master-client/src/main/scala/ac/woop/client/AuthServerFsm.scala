package ac.woop.client

import akka.util.ByteString
import io.enet.akka.{Shapper, Compressor}
import io.enet.akka.ENetService.{SendMessage, PacketFromPeer, PeerId}
import org.apache.commons.codec.binary.Hex
import Shapper._
import Compressor._

case class AuthServerFsm(serverKey: String) {
  case class ReceivedChallenge(pp: PacketFromPeer, challenge: String) {
    def challengeResponse = Hex.encodeHexString(digester.digest((serverKey + challenge).getBytes("UTF-8")))
    def responseMessage = pp.reply(1, challengeResponse)
    object IdentifiedCorrectly {
      def unapply(a: Any): Option[Unit] = PartialFunction.condOpt(a) {
        case PacketFromPeer(_, 0, 2 #:: ByteString.empty) => ()
      }
    }
    case class SendChallengeToClient(myChallenge: String) {
      val expectedResponse = Hex.encodeHexString(digester.digest((serverKey + myChallenge).getBytes("UTF-8")))
      def challengeMessage = pp.reply(3, myChallenge)
      case class GoodChallenge(respond: SendMessage)
      object CheckClientChallengeResponse {
        def unapply(a: Any): Option[GoodChallenge] = {
          PartialFunction.condOpt(a) {
            case pp @ PacketFromPeer(_, 0, 4 #:: `expectedResponse` ##:: ByteString.empty) =>
              GoodChallenge(pp.reply(5))
          }
        }
      }
    }
  }
  object ReceiveChallenge {
    def unapply(a: Any): Option[ReceivedChallenge] = {
      PartialFunction.condOpt(a) {
        case pp @ PacketFromPeer(_, 0, 0 #:: challenge ##:: ByteString.empty) =>
          ReceivedChallenge(pp, challenge)
      }
    }
  }
}
