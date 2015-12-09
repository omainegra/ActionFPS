package inter

import org.scalatest._

/**
  * Created by William on 09/12/2015.
  */
class InterSpec
  extends FunSuite
  with Matchers
  with Inspectors
  with OptionValues {
  test("it works") {
    val line = """Date: 2015-12-09T19:02:58.975Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: [127.0.0.1] |BC|I<3u says: '!inter'"""
    val InterCall(result) = line
    result.server shouldBe "aura.woop.ac:1999"
    result.nickname shouldBe "|BC|I<3u"
    result.ip shouldBe "127.0.0.1"
  }

}
