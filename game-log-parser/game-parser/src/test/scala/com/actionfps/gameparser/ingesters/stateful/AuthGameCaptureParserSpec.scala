package com.actionfps.gameparser.ingesters.stateful

import org.scalatest._

class AuthGameCaptureParserSpec
    extends WordSpec
    with Inside
    with Inspectors
    with Matchers
    with OptionValues {

  "Game capture for new version" must {

    "Not fail for this game" in {
      val expectedPlayers = Set(
        "rob" -> "Pi_Robtics",
        "frozzy" -> "Frozzy",
        "hex" -> "H3X|ZZ",
        "sanzo" -> "w00p|Sanzo",
        "drakas" -> "w00p|Drakas"
      )
      val expectedHosts = Set(
        "rob" -> "73.233.234.132",
        "frozzy" -> "63.235.43.23",
        "hex" -> "43.136.235.13",
        "sanzo" -> "93.135.43.3",
        "drakas" -> "133.232.232.83"
      )

      val expectedClans = Set(
        "hex" -> "zz",
        "sanzo" -> "woop",
        "drakas" -> "woop"
      )

      val inputSequence =
        """
          |Game status: ctf on ac_elevation, game finished, open, 5 clients
          |cn name             team flag  score frag death tk ping role    host
          | 0 Pi_Robtics       RVSF    5    704   37    23  0   40 normal  73.233.234.132:rob
          | 1 Frozzy           RVSF    2    206   15    22  1   41 normal  63.235.43.23:frozzy
          | 2 H3X|ZZ           CLA     9    771   32    27  0  363 normal  43.136.235.13:hex:zz
          | 3 w00p|Sanzo       CLA     1     28    0     4  1   10 normal  93.135.43.3:sanzo:woop
          | 5 w00p|Drakas      CLA     7    388   13    23  1  181 normal  133.232.232.83:drakas:woop
          |Team  CLA:  3 players,   45 frags,   17 flags
          |Team RVSF:  2 players,   52 frags,    7 flags
          |
          |Demo "Sun Apr 23 12:34:47 2017: ctf, ac_elevation, 582.80kB" recorded.
          |demo written to file "demos/20170423_1034_local_ac_elevation_15min_CTF.dmo.gz" (596791 bytes)
          |
        """.stripMargin.split("\r?\n")

      val outputs = inputSequence.scanLeft(GameBuilderState.initial)(_.next(_))

      val foundGame = outputs.collectFirst { case f: FoundGame => f }.value

      val haveUsers = foundGame.game.left.get.scores.flatMap { p =>
        p.user.map { u =>
          u -> p.name
        }
      }.toSet

      val haveClans = foundGame.game.left.get.scores.flatMap { p =>
        p.user.flatMap { u =>
          p.group.map { g =>
            u -> g
          }
        }
      }.toSet

      val haveHosts = foundGame.game.left.get.scores.flatMap { p =>
        p.user.map { u =>
          u -> p.host
        }
      }.toSet

      haveUsers shouldEqual expectedPlayers

      haveClans shouldEqual expectedClans

      haveHosts shouldEqual expectedHosts
    }

  }

}
