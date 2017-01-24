package userauth

import java.nio.file.Files

import org.scalatest.FunSuite

class AuthTest extends FunSuite {
  private lazy val tempFile = Files.createTempFile("auth", ".properties")
  private lazy val aap = AuthAtPath(tempFile)
  test("It works") {
    info(s"Temp file = ${tempFile}")
    val userId = "ahbeng"
    assert(aap.getPrivKey(userId).isEmpty)
    val newPrivKey = aap.getOrPutPrivKey(userId)
    assert(aap.getPrivKey(userId).get == newPrivKey)
  }

}
