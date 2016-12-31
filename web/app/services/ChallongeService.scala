package services

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.inject.ApplicationLifecycle
import providers.full.NewGameDetected
import tl.ChallongeClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 31/12/2016.
  */
@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient, applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private implicit val actorMaterializer = ActorMaterializer()

  /** Generate tournament IDs on demand **/

  private val gameDetectionActor = {
    Source
      .actorRef[NewGameDetected](10, OverflowStrategy.dropBuffer)
      .map(_.jsonGame)
      .via(ChallongeService.flow(challongeClient))
      .to(Sink.foreach(println))
      .run()
  }

  actorSystem.eventStream.subscribe(gameDetectionActor, classOf[NewGameDetected])

  applicationLifecycle.addStopHook(() => Future.successful(actorSystem.eventStream.unsubscribe(gameDetectionActor)))

}

object ChallongeService {

  def flow(challongeClient: ChallongeClient)(implicit executionContext: ExecutionContext): Flow[JsonGame, Int, NotUsed] = {
    val tournamentIdsSource = Source.repeat(()).mapAsync(1)(_ => challongeClient.fetchTournamentIds())
    Flow[JsonGame]
      .mapConcat(g => ChallongeService.detectWinnerLoserGame(g).toList)
      .zipWith(tournamentIdsSource) { case ((win, lose), tournamentIds) => tournamentIds.map { t => (t, win, lose) } }
      .mapConcat(identity)
      .mapAsync(3)(Function.tupled(challongeClient.attemptSubmit))
      .mapConcat(_.toList)
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

