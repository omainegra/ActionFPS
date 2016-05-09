package com.actionfps.ladder

import java.io.File
import java.net.URI
import java.time.format.DateTimeFormatter

import com.actionfps.ladder.parser.{LineParser, PlayerMessage}
import org.scalatest.{Matchers, WordSpec}

class LadderParserSpec extends WordSpec with Matchers {
  "it" must {
    "parse datetime" in {
      val missingYearParser = MissingYearParser(2016)
      val missingYearParser(date) = "Apr 27 12:12:36"

      DateTimeFormatter.ISO_ZONED_DATE_TIME.format(date) shouldBe "2016-04-27T12:12:36Z[UTC]"
    }
    "parse user message" in {
      val PlayerMessage(pm) = "[127.0.0.1] w00p|Drakas splattered cruising"
      pm.name shouldBe "w00p|Drakas"
    }
    "work" in {
      val ln = "May 02 03:29:38 [127.0.0.1] w00p|Drakas splattered cruising"
      val lp = LineParser(2016)
      val lp(dt, msg) = ln
      DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dt) shouldBe "2016-05-02T03:29:38Z[UTC]"
      msg shouldBe "[127.0.0.1] w00p|Drakas splattered cruising"

      lp.unapply("May 02 03:29:38") shouldBe empty
      lp.unapply("May 02 03:29:38 ").get._2 shouldBe ""

    }
    "fail for bad input" in {
      val lp = LineParser(2016)
      lp.unapply("ABCDEF          ") shouldBe empty
    }
  }

}