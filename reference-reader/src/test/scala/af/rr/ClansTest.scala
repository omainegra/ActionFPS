package af.rr

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
    val recs = ClanRecord.parseRecords(getSample("clans.csv"))
    recs should have size 30
    recs should contain(ClanRecord(
      id = "woop",
      shortName = "w00p",
      longName = "Woop Clan",
      website = Some(new URI("http://woop.us/")),
      tag = "w00p|*",
      tag2 = None,
      logo = Some(new URI("http://i.imgur.com/AnsEc0M.png"))
    ))
    recs.find(_.id == "rc").value.website.value.toString shouldBe "http://585437.xobor.com/"
  }
}
