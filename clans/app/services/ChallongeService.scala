package services

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.clans.CompleteClanwar
import play.api.Logger
import services.ChallongeService.NewClanwarCompleted
import tl.{ChallongeClient, WinFlow}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem) {

  Logger.info(s"Challonge service started.")

  private implicit val actorMaterializer = ActorMaterializer()

  Source
    .actorRef[NewClanwarCompleted](bufferSize = 10,
                                   OverflowStrategy.dropBuffer)
    .mapMaterializedValue(
      actorSystem.eventStream.subscribe(_, classOf[NewClanwarCompleted]))
    .map(_.clanwarCompleted)
    .filter { clanwar =>
      // don't allow old clanwars to be committed
      // todo add a journal for clanwar persistence
      ZonedDateTime
        .parse(clanwar.id)
        .isAfter(ZonedDateTime.now().minusHours(3))
    }
    .mapConcat(cc => WinFlow.detectWinnerLoserClanwar(cc).toList)
    .via(WinFlow(challongeClient).clanwarWon)
    .runWith(Sink.foreach(item => Logger.info(s"Sunk clanwar: ${item}")))
    .onComplete {
      case Success(_) => Logger.info("Challonge Service flow completed.")
      case Failure(reason) =>
        Logger.error(s"Challonge Service flow to: ${reason}", reason)
    }

}

object ChallongeService {
  case class NewClanwarCompleted(clanwarCompleted: CompleteClanwar)
}
