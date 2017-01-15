package services

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import providers.full.NewClanwarCompleted
import tl.{ChallongeClient, WinFlow}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 31/12/2016.
  *
  * Take Clanwars [[NewClanwarCompleted]] into [[ChallongeClient]] via [[WinFlow]].
  *
  * Publishes Clanwar results to Challonge.
  *
  */
@Singleton
class ChallongeService @Inject()(challongeClient: ChallongeClient, applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  private implicit val actorMaterializer = ActorMaterializer()

  private def subscribeActor(channel: Class[_])(actorRef: ActorRef): Unit = {
    actorSystem.eventStream.subscribe(actorRef, channel)
    applicationLifecycle.addStopHook(() => Future.successful(actorSystem.eventStream.unsubscribe(actorRef)))
  }

  private val winFlow = WinFlow(challongeClient)

  subscribeActor(classOf[NewClanwarCompleted]) {
    Source
      .actorRef[NewClanwarCompleted](10, OverflowStrategy.dropBuffer)
      .map(_.clanwarCompleted)
      .via(winFlow.clanwarAny)
      .to(Sink.foreach(item => Logger.info(s"Sunk clanwar: ${item}")))
      .run()
  }

}


