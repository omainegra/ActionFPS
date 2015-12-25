package actionfps
package master
package client
package authentication

import akka.util.ByteString
import io.enet.akka.Compressor._
import io.enet.akka.ENetService.{PacketFromPeer, SendMessage}
import io.enet.akka.Shapper._
import org.apache.commons.codec.binary.Hex

case class AuthServerFsm(serverKey: String) {

  case class ReceivedChallenge(pp: PacketFromPeer, challenge: String) {

    val IAmIdentifiedCorrectly = packetUnapplyS {
      case `tellServerChallengeIsGood` #:: ByteString.empty => ()
    }

    def challengeResponse = Hex.encodeHexString(digester.digest((serverKey + challenge).getBytes("UTF-8")))

    def responseMessage = pp.reply(serverRespondsToChallengeCode, challengeResponse)

    case class SendChallengeToClient(myChallenge: String) {

      val expectedResponse = Hex.encodeHexString(digester.digest((serverKey + myChallenge).getBytes("UTF-8")))

      val CheckClientChallengeResponse = packetUnapply {
        case (pp2, `clientResponseToChallengeCode` #:: `expectedResponse` ##:: ByteString.empty) =>
          GoodChallenge(pp2.reply(serverConfirmsGoodChallenge))
      }

      def challengeMessage = pp.reply(challengeToClientCode, myChallenge)

      case class GoodChallenge(respond: SendMessage)

    }

  }

  val ReceiveChallenge = packetUnapply {
    case (pp, `challengeToServerCode` #:: challenge ##:: ByteString.empty) =>
      ReceivedChallenge(pp, challenge)
  }

}
