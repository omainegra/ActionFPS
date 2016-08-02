package com.actionfps.reference

import org.scalatest.{Matchers, FunSuite}

/**
  * Created by William on 05/12/2015.
  */
class HeadingsTest
  extends FunSuite
    with Matchers {
  test("should work") {
    HeadingsRecord.parseRecords(getSample("headings.csv")) should have size 3
  }
}
