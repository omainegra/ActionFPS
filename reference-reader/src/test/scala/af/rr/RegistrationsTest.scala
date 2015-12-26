package af.rr

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
}
