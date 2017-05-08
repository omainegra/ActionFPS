package com.actionfps.user

import java.io.{InputStreamReader, StringReader}
import java.time.LocalDateTime
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class RegistrationsTest extends FunSuite with Matchers {

  private def registrationsReader = {
    new InputStreamReader(getClass.getResourceAsStream("registrations.csv"))
  }

  test("it should work") {
    Registration
      .parseRecords(registrationsReader) should have size 96
  }
  test("it should extract with e-mails") {
    val recs = Registration.parseRecords(registrationsReader)
    recs.head shouldBe Registration(
      "sanzo",
      "Sanzo",
      RegistrationEmail.PlainRegistrationEmail("sanzo@actionfps.com"),
      LocalDateTime.parse("2015-01-14T11:25"),
      "w00p|Sanzo")
    recs should have size 96
  }
  test("It maps one to one") {
    val recs = Registration.parseRecords(registrationsReader)
    val csvStr = Registration.writeCsv(recs)
    withClue(s"$csvStr") {
      Registration.parseRecords(new StringReader(csvStr)) shouldEqual recs
    }
  }
}
