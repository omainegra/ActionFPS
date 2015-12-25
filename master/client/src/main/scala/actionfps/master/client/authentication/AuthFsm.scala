package actionfps
package master
package client
package authentication

import akka.util.ByteString
import io.enet.akka.Compressor._
import io.enet.akka.ENetService.{PacketFromPeer, PeerId}
import io.enet.akka.Shapper._
import org.apache.commons.codec.binary.Hex

case class AuthFsm(remote: PeerId, serverKey: String) {

  def randomChallenge = Hex.encodeHexString(scala.util.Random.nextString(32).toArray.map(_.toByte))

  case class SendChallenge(challengeToServer: String) {

    val expectedResponse = Hex.encodeHexString(digester.digest((serverKey + challengeToServer).getBytes("UTF-8")))

    def logMessage = s"Challenge $challengeToServer sent, expected response $expectedResponse"

    def outputMessage = SendMessageAddition(remote, 0)(challengeToServerCode, challengeToServer)

    val WrongResponse = packetUnapplyS {
      case `serverRespondsToChallengeCode` #:: gotResponse ##:: ByteString.empty =>
        gotResponse -> s"Wrong response received. Expected challenge response $expectedResponse, got response $gotResponse"
    }

    val CorrectlyIdentified = packetUnapply {
      case (pp, `serverRespondsToChallengeCode` #:: `expectedResponse` ##:: ByteString.empty) =>
        pp.reply(tellServerChallengeIsGood) -> "Server identified himself correctly."
    }

    class ReceivedChallenge(val pp: PacketFromPeer, val challenge: String) {
      val myResponse = Hex.encodeHexString(digester.digest((serverKey + challenge).getBytes("UTF-8")))

      val AwaitAuthentication = packetUnapplyS {
        case `serverConfirmsGoodChallenge` #:: ByteString.empty => "Authentication was successful."
      }

      def logMessage = s"Server sent challenge $challenge, responding with $myResponse"

      def outputMessage = pp.reply(clientResponseToChallengeCode, myResponse)
    }

    val ReceivedChallenge = packetUnapply {
      case (pp, `challengeToClientCode` #:: challenge ##:: ByteString.empty) =>
        new ReceivedChallenge(pp, challenge)
    }

  }



}
