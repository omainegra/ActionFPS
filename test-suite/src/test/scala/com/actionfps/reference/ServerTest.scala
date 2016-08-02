package com.actionfps.reference

import org.scalatest.{Matchers, FunSuite}

/**
  * Created by William on 05/12/2015.
  */
class ServerTest
  extends FunSuite
    with Matchers {
  test("Should work") {
    ServerRecord.parseRecords(getSample("servers.csv")) should have size 8
  }
}
