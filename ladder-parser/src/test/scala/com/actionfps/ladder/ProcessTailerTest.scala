package com.actionfps.ladder

import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by me on 29/07/2016.
  */
class ProcessTailerTest extends FunSuite {

  test("It works") {
    var result = ""
    val t = new ProcessTailer(List("echo", "test"))(line => result = line)
    Thread.sleep(2000)
    result shouldBe "test"
  }

}
