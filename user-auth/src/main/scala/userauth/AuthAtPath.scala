package userauth

import java.io.{FileReader, FileWriter}
import java.nio.file.Path
import java.security.{KeyPairGenerator, SecureRandom}
import java.util.Properties
import javax.xml.bind.DatatypeConverter


// https://docs.oracle.com/javase/tutorial/security/apisign/step2.html
// https://gist.github.com/Paul255/6c4dec4f13ef03f6579f

case class AuthAtPath(path: Path) {
  def load(): Properties = {
    val properties = new Properties()
    val fr = new FileReader(path.toFile)
    try {
      properties.load(fr)
      properties
    } finally fr.close()
  }

  def putUser(id: String, privKey: String, pubKey: String): Unit = {
    val properties = load()
    properties.put(s"${id}.public-key", pubKey)
    properties.put(s"${id}.private-key", privKey)
    val fw = new FileWriter(path.toFile)
    try properties.store(fw, null)
    finally fw.close()
  }

  def getPrivKey(id: String): Option[String] = {
    Option(load().getProperty(s"${id}.private-key"))
  }

  def getOrPutPrivKey(id: String): String = {
    getPrivKey(id).getOrElse {
      val (priv, pub) = AuthAtPath.generatePair()
      putUser(id, priv, pub)
      getPrivKey(id).get
    }
  }
}

object AuthAtPath {
  private[userauth] def generatePair(): (String, String) = {
    val kpg = KeyPairGenerator.getInstance("DSA")
    val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
    kpg.initialize(1024, random)
    val pair = kpg.generateKeyPair()
    val priv = pair.getPrivate
    val pub = pair.getPublic
    (DatatypeConverter.printHexBinary(priv.getEncoded), DatatypeConverter.printHexBinary(pub.getEncoded))
  }

}
