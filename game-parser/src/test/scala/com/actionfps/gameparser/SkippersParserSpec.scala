package com.actionfps.gameparser

import com.actionfps.gameparser.ingesters.{FlagGameBuilder, FoundGame, NothingFound, ParserState}
import org.scalatest._

class SkippersParserSpec
  extends WordSpec
    with Matchers
    with Inspectors
    with Inside
    with OptionValues {

  "skippers" must {
    "Parse quitters at intermission" in {

      val inputSequence = {
        val src = scala.io.Source.fromInputStream(getClass.getResourceAsStream("skippers.txt"))
        try src.getLines().toList
        finally src.close()
      }.map { line =>
        line.replaceFirst(".*Payload: ?", "")
      }

      val outputs = inputSequence.scanLeft(NothingFound: ParserState)(_.next(_))
      outputs.foreach(println)

      val foundGame = outputs.find(_.isInstanceOf[FoundGame]).value

      inside(outputs(outputs.size - 2)) {
        case FoundGame(header, Left(flagGame)) =>
          inside(flagGame) {
            case FlagGameBuilder(_, scores, disconnectedScores, teamScores) =>
              val teamPlayers = scores.groupBy(_.team).mapValues(_.map(_.name))
              teamPlayers("RVSF") should contain only ("un")
              teamPlayers("CLA") should contain only("ZZ|CR7", "Morry=MyS=")
              teamPlayers should have size 2
              val teamPlayersDisconnected = disconnectedScores.groupBy(_.team).mapValues(_.map(_.name))
              teamPlayersDisconnected should have size 1
              teamPlayersDisconnected("CLA") should contain only "iR|.E"
          }
      }
    }
  }
}