package ac.woop.client

import akka.util.ByteString
import io.enet.akka.{Shapper, Compressor}
import io.enet.akka.ENetService.{SendMessage, PacketFromPeer, PeerId}
import org.apache.commons.codec.binary.Hex
import Shapper._
import Compressor._



case class AuthFsm(remote: PeerId, serverKey: String) {

  class PartialUnapply[To](f: PartialFunction[Any, To]) {
    def unapply(a: Any): Option[To] = f.lift.apply(a)
  }

  def partialUnapply[To](f: PartialFunction[Any, To]) = new PartialUnapply[To](f)

  def randomChallenge = Hex.encodeHexString(scala.util.Random.nextString(32).toArray.map(_.toByte))

  case class SendChallenge(challengeToServer: String) {

    val expectedResponse = Hex.encodeHexString(digester.digest((serverKey + challengeToServer).getBytes("UTF-8")))

    def logMessage = s"Challenge $challengeToServer sent, expected response $expectedResponse"

    def outputMessage = SendMessageAddition(remote, 0)(0, challengeToServer)

    class ReceivedChallenge(val pp: PacketFromPeer, val challenge: String) {
      val myResponse = Hex.encodeHexString(digester.digest((serverKey + challenge).getBytes("UTF-8")))

      val AwaitAuthentication = partialUnapply {
        case PacketFromPeer(`remote`, 0, 5 #:: ByteString.empty) => "Authentication was successful."
      }
      def logMessage = s"Server sent challenge $challenge, responding with $myResponse"
      def outputMessage = pp.reply(4, myResponse)

    }

    val ReceivedChallenge = partialUnapply {
      case pp@PacketFromPeer(`remote`, 0, 3 #:: challenge ##:: ByteString.empty) =>
        new ReceivedChallenge(pp, challenge)
    }

    val WrongResponse = partialUnapply {
      case pp@PacketFromPeer(`remote`, 0, 1 #:: gotResponse ##:: ByteString.empty) =>
        gotResponse -> s"Wrong response received. Expected challenge response $expectedResponse, got response $gotResponse"
    }

    val CorrectlyIdentified = partialUnapply {
      case pp@PacketFromPeer(`remote`, 0, 1 #:: `expectedResponse` ##:: ByteString.empty) =>
        pp.reply(2) -> "Server identified himself correctly."
    }

  }

}
