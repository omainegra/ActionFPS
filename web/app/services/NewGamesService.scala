package services

/**
  * Created by William on 28/12/2015.
  */

import javax.inject._

import acleague.enrichers.JsonGame
import akka.actor.ActorSystem
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsString, Json}

import concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class NewGamesService @Inject()(phpRenderService: PhpRenderService,
                                applicationLifecycle: ApplicationLifecycle)(
                                 implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  val (newGamesEnum, newGamesChan) = Concurrent.broadcast[Event]

  def accept(jsonGame: JsonGame): Unit = {
    phpRenderService.renderStatelessRaw("/live/render-fragment.php", jsonGame.toJsonNew).foreach(html =>
      newGamesChan.push(Event(
        id = Option(jsonGame.id),
        data = jsonGame.toJsonNew.+("html" -> JsString(html)).toString(),
        name = Option("new-game")
      ))
    )
  }

  val kal = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(newGamesChan.push(Event("")))
  applicationLifecycle.addStopHook(() => Future.successful(kal.cancel()))

}
