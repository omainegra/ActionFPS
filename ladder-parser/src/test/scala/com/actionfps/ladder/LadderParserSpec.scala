package com.actionfps.ladder

import java.time._
import java.time.format.DateTimeFormatter

import org.scalatest.{Matchers, WordSpec}

class LadderParserSpec extends WordSpec with Matchers {
  "it" must {
    "parse datetime" in {
      val result = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss").parse("Apr 27 12:12:36")
      val wat = Year.of(2016).atMonthDay(MonthDay.from(result)).atTime(LocalTime.from(result).atOffset(ZoneOffset.of("+00:00"))).atZoneSameInstant(ZoneId.of("UTC"))
      DateTimeFormatter.ISO_ZONED_DATE_TIME.format(wat) shouldBe "2016-04-27T12:12:36Z[UTC]"
    }
    "work" in {
      val f = "serverlog_20160427_12.12.36_local#28763.txt"
//      scala.io.Source.fromFile(f).

    }
  }

}