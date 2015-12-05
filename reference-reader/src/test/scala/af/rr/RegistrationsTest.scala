package af.rr

import java.io.InputStreamReader

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class RegistrationsTest
  extends FunSuite
  with Matchers {
  test("it should work") {
    Registration.parseRecords(new InputStreamReader(getClass.getResourceAsStream("registrations.csv"))) should have size 3
  }
}
