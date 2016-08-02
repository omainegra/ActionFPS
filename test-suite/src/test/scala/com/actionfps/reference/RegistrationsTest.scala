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
    info(s"Result first line = ${result.split("\n").head}")
    info(s"Result second line = ${result.split("\n")(1)}")
    info(s"Result 90th line = ${result.split("\n")(90)}")
    val recs = Registration.parseRecords(new StringReader(result))
    recs.head shouldBe Registration("sanzo", "Sanzo", None, LocalDateTime.parse("2015-01-14T11:25"))
    recs should have size 96
  }
}
