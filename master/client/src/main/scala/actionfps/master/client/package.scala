package actionfps
package master

import java.security.{MessageDigest, SecureRandom, Security}

import akka.util.ByteString
import io.enet.akka.ENetService.{PacketFromPeer, PeerId}
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.jce.provider.BouncyCastleProvider

package object client {

  class PartialUnapply[To](f: PartialFunction[Any, To]) {
    def unapply(a: Any): Option[To] = f.lift.apply(a)
  }

  def partialUnapply[To](f: PartialFunction[Any, To]) = {
    new PartialUnapply[To](f)
  }

  def packetUnapply[To](f: PartialFunction[(PacketFromPeer, ByteString), To]) = partialUnapply {
    case pp@PacketFromPeer(_, 0, bs) if f.isDefinedAt(pp -> bs) => f(pp -> bs)
  }

  def packetUnapplyS[To](f: PartialFunction[ByteString, To]) = partialUnapply {
    case pp@PacketFromPeer(_, 0, bs) if f.isDefinedAt(bs) => f(bs)
  }

  // http://stackoverflow.com/a/2208446
  Security.addProvider(new BouncyCastleProvider())
  val digester = MessageDigest.getInstance("SHA-256", "BC")

  def randomKey = {
    val random = new SecureRandom()
    val arr = Array.fill(20)(1.toByte)
    random.nextBytes(arr)
    val key = digester.digest(arr)
    Hex.encodeHexString(key)
  }

  private val sidMatch = s"""(.+):(\\d+)""".r

  def serverIdToPeerId(serverId: String): PeerId =
    serverId match {
      case sidMatch(host, port) => PeerId(host, port.toInt)
    }

  case object Authenticated
}
