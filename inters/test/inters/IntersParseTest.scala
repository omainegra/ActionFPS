package af.inters

import af.inters.IntersFlow.NicknameToUser
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created by me on 18/01/2017.
  */
class IntersParseTest extends FreeSpec with Matchers {
  "A UserMessage" - {
    "is produced from a syslog line" in {
      IntersFlow
        .UserMessageFromLine(new NicknameToUser {
          override def userOf(nickname: String): Option[String] =
            if (nickname == "w00p|Boo") Some("boo") else None
        })
        .unapply(IntersParseTest.syslogEvent) shouldBe defined
    }
    "is not produced when user doesn't map" in {
      IntersFlow
        .UserMessageFromLine(NicknameToUser.empty)
        .unapply(IntersParseTest.syslogEvent) shouldBe empty
    }
    "is not produced for empty input" in {
      IntersFlow
        .UserMessageFromLine(NicknameToUser.empty)
        .unapply("") shouldBe empty
    }
  }
}

object IntersParseTest {
  val syslogEvent: String = {
    """Date: 2017-01-17T15:10:13.942Z, Server: 62-210-131-155.rev.poneytelecom.eu """ +
      """sd-55104 AssaultCube[local#2999], Payload: [168.45.30.115] w00p|Boo says: '!inter'"""
  }
}
