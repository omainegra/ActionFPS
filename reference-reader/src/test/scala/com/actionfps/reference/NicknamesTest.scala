package com.actionfps.reference

import org.scalatest.{FunSuite, Matchers, OptionValues}

/**
  * Created by William on 05/12/2015.
  */
class NicknamesTest extends FunSuite with Matchers with OptionValues {

  test("It should parse both") {
    val r = NicknameRecord.parseRecords(getSample("nicknames.csv"))
    r should have size 139
  }

}
