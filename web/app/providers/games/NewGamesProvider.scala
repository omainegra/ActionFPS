package providers
package games

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.JsonGame
import controllers.Common
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import play.api.libs.streams.Streams
import play.api.{Configuration, Logger}
import views.rendergame.MixedGame

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class NewGamesProvider @Inject()(applicationLifecycle: ApplicationLifecycle,
                                 configuration: Configuration,
                                 common: Common,
                                 gamesProvider: GamesProvider)
                                (implicit actorSystem: ActorSystem,
                                 executionContext: ExecutionContext) {

  private val (newGamesEnum, thing) = Concurrent.broadcast[Event]
  val newGamesSource: Source[Event, NotUsed] = Source.fromPublisher(Streams.enumeratorToPublisher(newGamesEnum))
  private val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))

  private val logger = Logger(getClass)
  logger.info("Starting new games provider")

  def processGame(game: JsonGame): Unit = {
    val b = Json.toJson(game.withoutHosts).asInstanceOf[JsObject].+("isNew" -> JsBoolean(true))

    val gameHtml = views.rendergame.Render.renderMixedGame(MixedGame.fromJsonGame(game))
    thing.push(
      Event(
        id = Option(game.id),
        name = Option("new-game"),
        data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(gameHtml.body)).toString()
      )
    )
  }

  gamesProvider.addAutoRemoveHook(applicationLifecycle)(processGame)
  applicationLifecycle.addStopHook(() => Future.successful(keepAlive.cancel()))

}
