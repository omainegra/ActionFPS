package af.rr

import java.io.InputStreamReader
import java.net.URI

import org.scalatest.{OptionValues, FunSuite, Matchers}

/**
  * Created by William on 05/12/2015.
  */
class ClansTest
  extends FunSuite
  with Matchers
  with OptionValues {
  test("It should work") {
    val recs = ClanRecord.parseRecords(new InputStreamReader(getClass.getResourceAsStream("clans.csv")))
    recs should have size 5
    recs should contain(ClanRecord(
      id = "woop",
      shortName = "Woop",
      longName = "Woop Clan",
      website = Some(new URI("http://woop.us/")),
      tag = "w00p|*",
      tag2 = None,
      logo = Some(new URI("http://woop.us/logo.svg"))
    ))
    recs.find(_.id == "rc").value.website shouldBe empty
  }
}
