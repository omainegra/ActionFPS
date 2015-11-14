import lib.clans.{Clan, ResourceClans}
import org.scalatest.{Inspectors, Matchers, WordSpec}

class ClansSpec
  extends WordSpec
  with Matchers
  with Inspectors {

  "clan parser" must {
    "parse all clans" in {
      val woop = ResourceClans.clans("woop")
      woop.id shouldBe "woop"
      woop.nicknameInClan("w00p|Drakas") shouldBe true
      woop.nicknameInClan("|w00p|Drakas") shouldBe false
    }
  }

}