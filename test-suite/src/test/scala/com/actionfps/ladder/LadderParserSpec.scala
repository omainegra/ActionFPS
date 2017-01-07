package com.actionfps.ladder

import java.time.format.DateTimeFormatter

import com.actionfps.ladder.parser._
import org.scalatest.{Matchers, WordSpec}

class LadderParserSpec extends WordSpec with Matchers {
  "it" must {
    "parse a server based line" in {
      val ln = "server=woop.ac:10000 May 02 03:29:38 [127.0.0.1] w00p|Drakas splattered cruising"
      val ServerBasedLine(sbl) = ln
      sbl.server shouldEqual "woop.ac:10000"
      sbl.rest shouldEqual "May 02 03:29:38 [127.0.0.1] w00p|Drakas splattered cruising"
    }
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
    val input = """May 01 15:15:44 [109.252.202.117] w00p|Drakas logged in (default), AC: 1202|c24
                  |May 01 15:16:10 Status at 01-05-2016 15:16:10: 1 remote clients, 0.0 send, 0.3 rec (K/sec); Ping: #39|2490|126; CSL: #5|338|65 (bytes)
                  |May 01 15:16:58 [171.228.253.170] client connected
                  |[9.105.40.7] w00p|Sanzo busted w00p|Lucas
                  |Status at 03-07-2016 12:02:30: 2 remote clients, 1.3 send, 1.1 rec (K/sec); Ping: #89|5948|214; CSL: #36|2605|223 (bytes)
                  |[18.231.137.36] w00p|Lucas punctured w00p|Sanzo""".stripMargin
    // todo separate/tidy up
    "work for non-timestamped data" in {
      val lines = input.split("\n").flatMap(l => DirectTimedLine.unapply(l))
      lines(0).lineTiming shouldBe a [LineTiming.LocalDateWithoutYear]
      lines(0).message should startWith ("[109")
      lines(1).lineTiming shouldBe a [LineTiming.ExactLocalDT]
      lines(1).message should startWith ("Status")
      lines(2).lineTiming shouldBe a [LineTiming.LocalDateWithoutYear]
      lines(2).message should startWith ("[171")
      lines(3).lineTiming shouldBe LineTiming.NoExactTime
      lines(3).message should startWith ("[9")
      lines(4).lineTiming shouldBe a [LineTiming.ExactLocalDT]
      lines(4).message should startWith ("Stat")
      lines(5).lineTiming shouldBe LineTiming.NoExactTime
      lines(5).message should startWith ("[18")
    }

    "automatically reestablish time after getting information" in {
      val lines: List[DirectTimedLine] = input.split("\n").flatMap(l => DirectTimedLine.unapply(l)).toList
      val results = lines.scanLeft(LineTimerScanner.empty)(_.include(_)).flatMap(_.emitLine)
      results(0).scannedTiming shouldBe ScannedTiming.NoTime
      results(1).scannedTiming shouldBe a[ScannedTiming.After]
      results(2).scannedTiming shouldBe a[ScannedTiming.After]
      results(3).scannedTiming shouldBe a[ScannedTiming.After]
      results(4).scannedTiming shouldBe a[ScannedTiming.After]
      results(4).scannedTiming shouldNot be (results(3).scannedTiming)
      results(5).scannedTiming shouldBe a[ScannedTiming.After]
    }
  }

}
