package com.actionfps
package gameparser
package ingesters
package stateful

import fastparse.all._
import org.scalatest._

class DemoCaptureParserSpec
    extends WordSpec
    with Inside
    with Inspectors
    with Matchers
    with OptionValues {

  "Demo capture" must {

    "parse demo written" in {
      val input =
        """demo written to file "demos/20141214_1547_local_ac_aqueous_4min_TDM.dmo" (115739 bytes)"""
      val DemoWritten(dw) = input
      dw.filename shouldEqual "demos/20141214_1547_local_ac_aqueous_4min_TDM.dmo"
      dw.size shouldEqual "115739 bytes"
    }

    "Parse date" in {
      SharedParsers.yearParser.parse("2014").get
      val input = "Thu Dec 18 19:24:56 2014"
      SharedParsers.timestampParser.parse(input).get
      val Parsed.Success(res, _) = SharedParsers.timestampParser.!.parse(input)
      res shouldEqual input
    }

    "Parse map" in {
      val input = "ac_mines"
      val Parsed.Success(res, _) = SharedParsers.mapNameParser.!.parse(input)
      res shouldEqual input
    }

    "Parse size" in {
      val input = "610.60kB"
      val Parsed.Success(res, _) = DemoRecorded.sizeParser.!.parse(input)
      res shouldEqual input
    }

    "Parse a basic bit" in {
      val input = "Thu Dec 18 19:24:56 2014: ctf, ac_gothic, 610.60kB"
      val Parsed.Success(res, _) = DemoRecorded.quotedBit.parse(input)
      res.dateTime shouldBe "Thu Dec 18 19:24:56 2014"
      res.mode shouldBe "ctf"
      res.map shouldBe "ac_gothic"
      res.size shouldBe "610.60kB"
    }
    "Not fail another demo" in {
      val inputSequence =
        """
          |Demo "Thu Dec 18 19:24:56 2014: ctf, ac_gothic, 610.60kB" recorded.
          |demo written to file "demos/20141218_1824_local_ac_gothic_15min_CTF.dmo" (625252 bytes)
          |
        """.stripMargin.split("\r?\n")

      val outputs =
        inputSequence.scanLeft(NoDemosCollected: DemoCollector)(_.next(_))

      forExactly(1, outputs) { output =>
        output shouldBe a[DemoWrittenCollected]
      }
    }
    "Not fail a simple demo (1.2)" in {
      val inputSequence =
        """
          |
          |Demo "Sun Dec 14 16:47:14 2014: team deathmatch, ac_aqueous, 113.03kB, 11 mr" recorded.
          |demo written to file "demos/20141214_1547_local_ac_aqueous_4min_TDM.dmo" (115739 bytes)
          |Map height density information for ac_aqueous: H = 17.44 V = 865894, A = 49648 and MA = 2548
          |
          |Game start: team deathmatch on ac_aqueous, 6 players, 15 minutes, mastermode 0, (map rev 22/19068, off
          |
        """.stripMargin.split("\r?\n")

      val outputs =
        inputSequence.scanLeft(NoDemosCollected: DemoCollector)(_.next(_))

      forExactly(1, outputs) { output =>
        output shouldBe a[DemoWrittenCollected]
        inside(output) {
          case DemoWrittenCollected(DemoRecorded(dateTime,
                                                 mode,
                                                 map,
                                                 sizeRecorded),
                                    DemoWritten(filename, sizeWritten)) =>
            dateTime shouldBe "Sun Dec 14 16:47:14 2014"
            mode shouldBe "team deathmatch"
            map shouldBe "ac_aqueous"
            sizeRecorded shouldBe "113.03kB"
            filename shouldBe "demos/20141214_1547_local_ac_aqueous_4min_TDM.dmo"
            sizeWritten shouldBe "115739 bytes"
        }
      }

    }
  }

}
