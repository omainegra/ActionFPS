package com.actionfps.inter

import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by me on 29/05/2016.
  */
class InterMessageTest extends FunSuite {
  test("It parses") {
    val input = "[12.2.3.4] Morry=MyS= says: '!inter'"
    val InterMessage(r) = input
    r.ip shouldBe "12.2.3.4"
    r.nickname shouldBe "Morry=MyS="
  }
}
