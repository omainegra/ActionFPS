package tl

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.actionfps.clans.CompleteClanwar
import tl.ChallongeClient.ClanwarWon

import scala.concurrent.ExecutionContext

/**
  * Created by me on 14/01/2017.
  */
case class WinFlow(challongeClient: ChallongeClient)(
    implicit executionContext: ExecutionContext) {

  def clanwarWon: Flow[ClanwarWon, Option[Int], NotUsed] = {
    Flow[ClanwarWon]
      .mapAsync(2) { clanwarWon =>
        challongeClient.fetchTournamentIds().map { ids =>
          ids.map { id =>
            id -> clanwarWon
          }
        }
      }
      .mapConcat(identity)
      .mapAsync(3) {
        case (i, w) =>
          challongeClient.attemptSubmit(i, w)
      }
  }

}

object WinFlow {

  val TestClanwarTournament = "af_test_tournament_clanwar"
  val TestGameTournament = "af_test_tournament"

  def detectWinnerLoserClanwar(cc: CompleteClanwar): Option[ClanwarWon] = {
    for {
      winnerId <- cc.winner
      loserId <- cc.clans.find(_ != winnerId)
      winnerScore <- cc.scores.get(winnerId)
      loserScore <- cc.scores.get(loserId)
    } yield ClanwarWon(cc.id, winnerId, winnerScore, loserId, loserScore)
  }

}
