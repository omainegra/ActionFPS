package com.actionfps.reference

import java.io.StringReader
import java.time.LocalDateTime

import com.actionfps.reference.Registration.Email.PlainEmail
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class RegistrationsTest
  extends FunSuite
    with Matchers {
  test("it should work") {
    Registration.parseRecords(getSample("registrations.csv")) should have size 96
  }
  test("it should extract with e-mails") {
    val recs = Registration.parseRecords(getSample("registrations.csv"))
    recs.head shouldBe Registration("sanzo", "Sanzo", PlainEmail("sanzo@actionfps.com"), LocalDateTime.parse("2015-01-14T11:25"), "w00p|Sanzo")
    recs should have size 96
  }
  test("It maps one to one") {
    val recs = Registration.parseRecords(getSample("registrations.csv"))
    val csvStr = Registration.writeCsv(recs)
    withClue(s"$csvStr") {
      Registration.parseRecords(new StringReader(csvStr)) shouldEqual recs
    }
  }
}
