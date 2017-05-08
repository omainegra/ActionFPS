package com.actionfps.servers

import java.io.InputStreamReader

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class ServerTest extends FunSuite with Matchers {
  private def reader =
    new InputStreamReader(getClass.getResourceAsStream(s"servers.csv"))

  test("Should work") {
    ServerRecord.parseRecords(reader) should have size 8
  }
}
