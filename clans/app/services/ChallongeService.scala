package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.clans.CompleteClanwar
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import services.ChallongeService.NewClanwarCompleted
import tl.{ChallongeClient, WinFlow}

import scala.concurrent.ExecutionContext

@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient,
                                 applicationLifecycle: ApplicationLifecycle)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem) {

  private implicit val actorMaterializer = ActorMaterializer()

  Source
    .actorRef[NewClanwarCompleted](bufferSize = 10,
                                   OverflowStrategy.dropBuffer)
    .mapMaterializedValue(
      actorSystem.eventStream.subscribe(_, classOf[NewClanwarCompleted]))
    .map(_.clanwarCompleted)
    .via(WinFlow(challongeClient).clanwarAny)
    .to(Sink.foreach(item => Logger.info(s"Sunk clanwar: ${item}")))
    .run()

}

object ChallongeService {
  case class NewClanwarCompleted(clanwarCompleted: CompleteClanwar)
}
