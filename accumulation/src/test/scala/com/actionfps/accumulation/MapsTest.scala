package com.actionfps.accumulation

import org.scalatest.Matchers._
import org.scalatest._

/**
  * Created by me on 18/01/2017.
  */
class MapsTest extends FunSuite {
  test("31 maps") {
    Maps.mapToImage.size should be > 25
  }
  test("ac_depot is there") {
    Maps.mapNames contains "ac_depot"
  }
  test("ac_depot image matches format") {
    Maps.mapToImage("ac_depot") should (startWith("https") and endWith(".jpg"))
  }
}
