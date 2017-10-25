package com.actionfps
package gameparser
package ingesters
package stateful

import org.scalatest._

class GameCaptureParserSpec
  extends WordSpec
    with Inside
    with Inspectors
    with Matchers
    with OptionValues {

  "Game parse" must {
    "Parse a game start line" in {
      val input = "Game start: ctf on ac_depot, 1 players, 15 minutes, mastermode 0, (map rev 3/15383, official, 'getmap' not prepared)"
      val result = GameStartHeader.unapply(input)
      result.value shouldEqual GameStartHeader(GameMode.CTF, "ac_depot", 1, 15)
    }
    "Parse a finish line" in {
      val input = "Game status: team deathmatch on ac_aqueous, game finished, open, 6 clients"
      val GameFinishedHeader(h) = input
      h.map shouldBe "ac_aqueous"
      h.state shouldBe "open"
    }
    "Parse in progress" in {
      val input = "Game status: hunt the flag on ac_depot, 14 minutes remaining, open, 4 clients"
      val GameInProgressHeader(h) = input
      h.map shouldBe "ac_depot"
      h.remaining shouldBe 14
    }
    "Parse frag game score" in {
      val line = " 0 Daimon           RVSF    -12    0     3  0   32 normal  2.12.186.32"
      val line2 = " 1 ~FEL~.RayDen     RVSF     57    8     2  0  169 normal  186.83.65.12"
      val TeamModes.FragStyle.IndividualScore(r) = line
      val TeamModes.FragStyle.IndividualScore(r2) = line2
    }
    "Parse flag game score" in {
      val line = "1 w00p|Lucas       RVSF    1    514   45    42  0  112 normal  138.231.142.200"
      val TeamModes.FlagStyle.IndividualScore(r) = line
    }
    "Parse team flag score" in {
      val line = "Team  CLA:  0 players,    0 frags,    0 flags"
      val TeamModes.FlagStyle.TeamScore(t) = line
    }
    "Parse a disconnected TDM score" in {
      val line = "   ~FEL~MR.JAM      RVSF    1    7       -    - disconnected"
      val TeamModes.FragStyle.IndividualScoreDisconnected(t) = line
    }

  }

}
