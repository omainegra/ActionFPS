package af.rr

import java.io.InputStreamReader

import org.scalatest.{Matchers, FunSuite}

/**
  * Created by William on 05/12/2015.
  */
class ServerTest
  extends FunSuite
  with Matchers {
  test("Should work") {
    ServerRecord.parseRecords(new InputStreamReader(getClass.getResourceAsStream("servers.csv"))) should have size 5
  }
}
