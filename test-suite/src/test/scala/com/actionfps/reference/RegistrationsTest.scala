package com.actionfps.reference

import java.io.StringReader
import java.time.LocalDateTime

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
  test("it should filter e-mails") {
    val result = Registration.filterRegistrationsEmail(getSample("registrations.csv"))
    val recs = Registration.parseRecords(new StringReader(result))
    every(recs.map(_.email)) shouldBe empty
    recs should have size 96
  }
  test("it should extract with e-mails") {
    val recs = Registration.parseRecords(getSample("registrations.csv"))
    recs.head shouldBe Registration("sanzo", "Sanzo", Some("sanzo@actionfps.com"), LocalDateTime.parse("2015-01-14T11:25"))
    recs should have size 96
  }
}
