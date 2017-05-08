import org.scalatest.OptionValues._
import org.scalatest.{FreeSpec, Matchers}
import services.LadderService
import services.LadderService.NickToUser

/**
  * Created by me on 18/01/2017.
  */
class LadderParseTest extends FreeSpec with Matchers {
  "TimedUserMessage" - {
    "is extracted" in {
      val tme = LadderService
        .TimedUserMessageExtract(new NickToUser {
          override def userOfNickname(nickname: String): Option[String] =
            if (nickname == "egg") Some("egghead") else None
        })
        .unapply(LadderParseTest.sampleMessage)
        .value
      assert(tme.gibbed)
      assert(!tme.scored)
      assert(!tme.killed)
      assert(tme.user == "egghead")
      assert(tme.message == "gibbed nescio")
    }
    "is not extracted when user doesn't match" in {
      LadderService
        .TimedUserMessageExtract(NickToUser.empty)
        .unapply(LadderParseTest.sampleMessage) shouldBe empty
    }
    "is not extracted for an empty input" in {
      LadderService
        .TimedUserMessageExtract(NickToUser.empty)
        .unapply("") shouldBe empty
    }
  }
}

object LadderParseTest {
  val sampleMessage =
    """2016-07-02T21:58:09 [92.21.240.78:egg] egg gibbed nescio"""
}
