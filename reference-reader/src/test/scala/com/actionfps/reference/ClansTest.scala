package com.actionfps.reference

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
    recs should have size 29
    recs should contain(ClanRecord(
      id = "woop",
      shortName = "w00p",
      longName = "Woop Clan",
      website = Some(new URI("http://woop.us/")),
      tag = "w00p|*",
      tag2 = None,
      logo = new URI("https://i.imgur.com/AnsEc0M.png"),
      teamspeak = Some(new URI("ts3server://abc.def:123"))
    ))
    recs.find(_.id == "rc").value.website.value.toString shouldBe "http://585437.xobor.com/"
  }
}
