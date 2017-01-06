package services

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import providers.full.{NewClanwarCompleted, NewGameDetected}
import tl.ChallongeClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 31/12/2016.
  */
@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient, applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private implicit val actorMaterializer = ActorMaterializer()

  private def subscribeActor(channel: Class[_])(actorRef: ActorRef): Unit = {
    actorSystem.eventStream.subscribe(actorRef, channel)
    applicationLifecycle.addStopHook(() => Future.successful(actorSystem.eventStream.unsubscribe(actorRef)))
  }

  private val winFlow = ChallongeService.WinFlow(challongeClient)

  subscribeActor(classOf[NewClanwarCompleted]) {
    Source
      .actorRef[NewClanwarCompleted](10, OverflowStrategy.dropBuffer)
      .map(_.clanwarCompleted)
      .via(winFlow.clanwarAny)
      .to(Sink.foreach(item => Logger.info(s"Sunk clanwar: ${item}")))
      .run()
  }

}

object ChallongeService {

  private val TestClanwarTournament = "af_test_tournament_clanwar"
  private val TestGameTournament = "af_test_tournament"

  case class WinFlow(challongeClient: ChallongeClient)(implicit executionContext: ExecutionContext) {
    private def clanwar: Flow[CompleteClanwar, Int, NotUsed] = {
      Flow[CompleteClanwar]
        .map(ChallongeService.detectWinnerLoserClanwar)
        .mapConcat(_.toList)
        .mapAsync(3)(Function.tupled(challongeClient.attemptSubmit(TestClanwarTournament, _, _)))
        .mapConcat(_.toList)
    }

    private def game: Flow[JsonGame, Int, NotUsed] = {
      Flow[JsonGame]
        .map(ChallongeService.detectWinnerLoserGame)
        .mapConcat(_.toList)
        .mapAsync(3)(Function.tupled(challongeClient.attemptSubmit(TestGameTournament, _, _)))
        .mapConcat(_.toList)
    }

    def gameAny: Flow[JsonGame, Int, NotUsed] = {
      /** Generate tournament IDs on demand **/
      val tournamentIdsSource = Source.repeat(()).mapAsync(1)(_ => challongeClient.fetchTournamentIds())
      Flow[JsonGame]
        .mapConcat(g => ChallongeService.detectWinnerLoserGame(g).toList)
        .zipWith(tournamentIdsSource) { case ((win, lose), tournamentIds) => tournamentIds.map { t => (t, win, lose) } }
        .mapConcat(identity)
        .mapAsync(3)(Function.tupled(challongeClient.attemptSubmit))
        .mapConcat(_.toList)
    }

    def clanwarAny: Flow[CompleteClanwar, Int, NotUsed] = {
      Flow[CompleteClanwar]
        .mapConcat(g => ChallongeService.detectWinnerLoserClanwar(g).toList)
        .mapAsync(2) { case (win, lose) =>
          challongeClient.fetchTournamentIds().map { ids => ids.map { id => (id, win, lose) } }
        }
        .mapConcat(identity)
        .mapAsync(3)(Function.tupled(challongeClient.attemptSubmit))
        .mapConcat(_.toList)
    }

  }

  def detectWinnerLoserClanwar(cc: CompleteClanwar): Option[(String, String)] = {
    for {
      winnerId <- cc.winner
      loserId <- cc.clans.find(_ != winnerId)
    } yield (winnerId, loserId)
  }

  def detectWinnerLoserGame(jsonGame: JsonGame): Option[(String, String)] = {
    for {
      winnerClan <- jsonGame.winnerClan
      loserClan <- jsonGame.clangame.flatMap(_.find(_ != winnerClan))
    } yield (winnerClan, loserClan)
  }
}

