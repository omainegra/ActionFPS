import org.scalatest.{FreeSpec, Matchers}
import services.IntersService

/**
  * Created by me on 18/01/2017.
  */
class IntersParseTest extends FreeSpec with Matchers {
  "A UserMessage" - {
    "is produced from a syslog line" in {
      IntersService
        .UserMessageFromLine(Map("w00p|Boo" -> "boo").get)
        .unapply(IntersParseTest.syslogEvent) shouldBe defined
    }
    "is not produced when user doesn't map" in {
      IntersService
        .UserMessageFromLine(Function.const(None))
        .unapply(IntersParseTest.syslogEvent) shouldBe empty
    }
    "is not produced for empty input" in {
      IntersService
        .UserMessageFromLine(Function.const(None))
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
