package com.actionfps.user

import java.security.{KeyPairGenerator, PrivateKey, PublicKey}

import com.actionfps.user.RegistrationEmail.{
  PlainRegistrationEmail,
  SecureRegistrationEmail
}
import org.scalatest._
import EitherValues._

/**
  * Created by William on 05/12/2015.
  */
class RegistrationEmailTest extends FunSuite with Matchers {

  private lazy val keyPair = {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(1024)
    keyPairGenerator.generateKeyPair()
  }

  private lazy implicit val privateKey: PrivateKey = keyPair.getPrivate
  private lazy implicit val publicKey: PublicKey = keyPair.getPublic

  private val sampleEmail = PlainRegistrationEmail("sanzo@woop.us")

  test("Secure e-mail validates as expected") {
    assert(sampleEmail.secured.matches(sampleEmail.email))
  }
  test("Secure e-mail does not contain the original e-mail") {
    sampleEmail.secured.toString should not include ("sanzo")
  }
  test("Secure e-mail is decrypted properly") {
    sampleEmail.secured.decrypt shouldEqual sampleEmail
  }
  test("Secure e-mail reparses") {
    RegistrationEmail
      .fromString(sampleEmail.secured.stringValue)
      .right
      .value
      .asInstanceOf[SecureRegistrationEmail]
      .decrypt shouldEqual sampleEmail.secured.decrypt
  }
  test("Normal e-mail reparses") {
    RegistrationEmail
      .fromString(sampleEmail.stringValue)
      .right
      .value shouldEqual sampleEmail
  }

}
