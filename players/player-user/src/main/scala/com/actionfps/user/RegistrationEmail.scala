package com.actionfps.user

import java.security.{MessageDigest, PrivateKey, PublicKey}
import javax.crypto.Cipher
import javax.xml.bind.DatatypeConverter

import com.actionfps.user.RegistrationEmail.SecureRegistrationEmail

/**
  * Created by me on 04/03/2017.
  */
object RegistrationEmail {

  private val cipherName = "RSA/ECB/PKCS1PADDING"

  final case class PlainRegistrationEmail(email: String)
      extends RegistrationEmail {
    override def stringValue: String = s"$mailto$email"

    override def matches(emailString: String): Boolean = emailString == email

    def hashedEmail: String = {
      val sha1 = MessageDigest.getInstance("SHA1")
      DatatypeConverter.printHexBinary(sha1.digest(email.getBytes()))
    }

    def secured(implicit publicKey: PublicKey): SecureRegistrationEmail = {
      val cipher = Cipher.getInstance(cipherName)
      cipher.init(Cipher.ENCRYPT_MODE, publicKey)
      SecureRegistrationEmail(
        encrypted =
          DatatypeConverter.printHexBinary(cipher.doFinal(email.getBytes())),
        hashed = hashedEmail
      )
    }
  }

  final case class SecureRegistrationEmail(encrypted: String, hashed: String)
      extends RegistrationEmail {
    override def stringValue: String = s"$secureEmailPrefix$encrypted:$hashed"

    override def matches(emailString: String): Boolean =
      hashed == PlainRegistrationEmail(emailString).hashedEmail

    def decrypt(implicit privateKey: PrivateKey): PlainRegistrationEmail = {
      val cipher = Cipher.getInstance(cipherName)
      cipher.init(Cipher.DECRYPT_MODE, privateKey)
      PlainRegistrationEmail(
        email = new String(
          cipher.doFinal(DatatypeConverter.parseHexBinary(encrypted))))
    }

    override def secured(
        implicit publicKey: PublicKey): SecureRegistrationEmail = this
  }

  val mailto = "mailto:"
  val secureEmailPrefix = "uri:actionfps.com:secure-email:"
  val secureEmailHashSeed = "secure-actionfps-email"

  def fromString(string: String): Either[String, RegistrationEmail] = {
    if (string.startsWith(secureEmailPrefix)) {
      string.drop(secureEmailPrefix.length).split(':').toList match {
        case encrypted :: hashed :: _ =>
          Right(SecureRegistrationEmail(encrypted, hashed))
        case _ =>
          Left("Could not parse secure e-mail")
      }
    } else if (string.startsWith(mailto))
      Right(PlainRegistrationEmail(string.drop(mailto.length)))
    else if (string.contains('@'))
      Right(PlainRegistrationEmail(string))
    else Left("Could not parse e-mail")
  }

}

sealed trait RegistrationEmail {
  def matches(emailString: String): Boolean

  def stringValue: String

  def secured(implicit publicKey: PublicKey): SecureRegistrationEmail
}
