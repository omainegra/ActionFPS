package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.inject.ApplicationLifecycle
import providers.full.NewGameDetected

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 31/12/2016.
  */
@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient, applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private implicit val actorMaterializer = ActorMaterializer()

  private val activeTournamentsF = challongeClient.fetchTournamentIds()

  def receiveClanwar(winnerClanId: String, loserClanId: String): Future[List[Int]] =
    for {
      activeTournaments <- activeTournamentsF
      matchIds <- Future.sequence(activeTournaments.map(challongeClient.attemptSubmit(_, winnerClanId, loserClanId))).map(_.flatten)
    } yield matchIds

  private val gameDetectionActor = Source.actorRef[NewGameDetected](10, OverflowStrategy.dropBuffer)
    .mapConcat(g => ChallongeService.detectWinnerLoserGame(g.jsonGame).toList)
    .mapAsync(1)(Function.tupled(receiveClanwar))
    .to(Sink.foreach(println))
    .run()

  actorSystem.eventStream.subscribe(gameDetectionActor, classOf[NewGameDetected])
  applicationLifecycle.addStopHook(() => Future.successful(actorSystem.eventStream.unsubscribe(gameDetectionActor)))

}

object ChallongeService {

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

