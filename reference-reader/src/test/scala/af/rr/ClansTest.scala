package af.rr

import java.io.InputStreamReader
import java.net.URI

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class ClansTest
  extends FunSuite
  with Matchers {
  test("It should work") {
    val recs = ClanRecord.parseRecords(new InputStreamReader(getClass.getResourceAsStream("clans.csv")))
    recs should have size 5
    recs should contain(ClanRecord(
      id = "woop",
      shortName = "Woop",
      longName = "Woop Clan",
      website = Some(new URI("http://woop.us/")).filter(_.getScheme != null),
      tag = "w00p|*",
      tag2 = None,
      logo = Some(new URI("http://woop.us/logo.svg"))
    ))
  }
}
