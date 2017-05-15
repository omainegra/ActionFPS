package tl

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.actionfps.clans.CompleteClanwar
import com.actionfps.api.{Game => JsonGame}

import scala.concurrent.ExecutionContext

/**
  * Created by me on 14/01/2017.
  */
case class WinFlow(challongeClient: ChallongeClient)(
    implicit executionContext: ExecutionContext) {
  private def clanwar: Flow[CompleteClanwar, Int, NotUsed] = {
    Flow[CompleteClanwar]
      .map(WinFlow.detectWinnerLoserClanwar)
      .mapConcat(_.toList)
      .mapAsync(3)(Function.tupled(challongeClient
        .attemptSubmit(WinFlow.TestClanwarTournament, _, _, _, _)))
      .mapConcat(_.toList)
  }

  private def game: Flow[JsonGame, Int, NotUsed] = {
    Flow[JsonGame]
      .map(WinFlow.detectWinnerLoserGame)
      .mapConcat(_.toList)
      .mapAsync(3)(Function.tupled(
        challongeClient.attemptSubmit(WinFlow.TestGameTournament, _, _, _, _)))
      .mapConcat(_.toList)
  }

  def gameAny: Flow[JsonGame, Int, NotUsed] = {

    /** Generate tournament IDs on demand **/
    val tournamentIdsSource =
      Source.repeat(()).mapAsync(1)(_ => challongeClient.fetchTournamentIds())
    Flow[JsonGame]
      .mapConcat(g => WinFlow.detectWinnerLoserGame(g).toList)
      .zipWith(tournamentIdsSource) {
        case ((win, ws, lose, ls), tournamentIds) =>
          tournamentIds.map { t =>
            (t, win, ws, lose, ls)
          }
      }
      .mapConcat(identity)
      .mapAsync(3)(Function.tupled(
        challongeClient.attemptSubmit(_, _, _, _, _)))
      .mapConcat(_.toList)
  }

  def clanwarAny: Flow[CompleteClanwar, Int, NotUsed] = {
    Flow[CompleteClanwar]
      .mapConcat(ccw =>
        WinFlow.detectWinnerLoserClanwar(ccw).toList.map(s => ccw -> s))
      .mapAsync(2) {
        case (ccw, (win, ws, lose, ls)) =>
          challongeClient.fetchTournamentIds().map { ids =>
            ids.map { id =>
              (id, win, ws, lose, ls, Option(ccw.id))
            }
          }
      }
      .mapConcat(identity)
      .mapAsync(3) {
        case (i, w, ws, l, ls, sid) =>
          challongeClient.attemptSubmit(i, w, ws, l, ls, sid)
      }
      .mapConcat(_.toList)
  }

}

object WinFlow {

  val TestClanwarTournament = "af_test_tournament_clanwar"
  val TestGameTournament = "af_test_tournament"

  def detectWinnerLoserClanwar(
      cc: CompleteClanwar): Option[(String, Int, String, Int)] = {
    for {
      winnerId <- cc.winner
      loserId <- cc.clans.find(_ != winnerId)
      winnerScore <- cc.scores.get(winnerId)
      loserScore <- cc.scores.get(loserId)
    } yield (winnerId, winnerScore, loserId, loserScore)
  }

  def detectWinnerLoserGame(
      jsonGame: JsonGame): Option[(String, Int, String, Int)] = {
    for {
      winnerClan <- jsonGame.winnerClan
      winnerScore <- jsonGame.teams
        .find(_.clan.contains(winnerClan))
        .map(t => t.flags.getOrElse(t.frags))
      loserClan <- jsonGame.clangame.flatMap(_.find(_ != winnerClan))
      loserScore <- jsonGame.teams
        .find(_.clan.contains(loserClan))
        .map(t => t.flags.getOrElse(t.frags))
    } yield (winnerClan, winnerScore, loserClan, loserScore)
  }
}
