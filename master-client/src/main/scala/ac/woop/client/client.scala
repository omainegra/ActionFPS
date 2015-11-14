package ac.woop

import java.security.{SecureRandom, MessageDigest, Security}

import io.enet.akka.ENetService.PeerId
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.jce.provider.BouncyCastleProvider

package object client {

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

}