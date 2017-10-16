package com.actionfps.user

import java.io.InputStreamReader

import org.scalatest._

/**
  * Created by William on 05/12/2015.
  */
class NicknamesTest extends FunSuite with Matchers with OptionValues {

  test("It should parse both") {
    NicknameRecord.parseRecords(new InputStreamReader(
      getClass.getResourceAsStream("nicknames.tsv"))) should have size 139
  }

}
