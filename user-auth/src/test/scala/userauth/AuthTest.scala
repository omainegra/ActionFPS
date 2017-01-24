package userauth

import java.nio.file.{FileAlreadyExistsException, Files, Paths}

import org.scalatest.FunSuite

class AuthTest extends FunSuite {
  test("It works") {
    val (priv, pub) = Ah.generatePair()
    info(priv)
    info(pub)
  }
  test("Ser deser works") {
    val im = Map("x" -> List("x", "y"), "z" -> List("123"))
    assert(Ah.parseValues(Ah.serializeValues(im)) == im)
  }
  test("File thing works") {
    val path = Paths.get("test.db")
    try Files.createFile(path)
    catch { case _: FileAlreadyExistsException => }
    val userDb = Map("id" -> List("x"), "group" -> List("x", "y"))
    Ah.putUser("x", path, userDb)
    val u = Ah.getUser("x", path)
    assert(u.get == userDb)
  }

}
