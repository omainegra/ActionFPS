package af.rr

import java.io.InputStreamReader

import org.scalatest.{FunSuite, Matchers, OptionValues}

/**
  * Created by William on 05/12/2015.
  */
class NicknamesTest extends FunSuite with Matchers with OptionValues {

  test("It should parse both") {
    val r = NicknameRecord.parseRecords(new InputStreamReader(getClass.getResourceAsStream("nicknames.csv")))
    r should have size 9
  }

}
