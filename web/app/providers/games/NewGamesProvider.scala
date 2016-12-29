package providers
package games

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Source
import com.actionfps.gameparser.Maps
import com.actionfps.gameparser.enrichers.JsonGame
import akka.actor.ActorSystem
import com.actionfps.accumulation.EnrichGames
import controllers.Common
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import play.api.libs.streams.Streams
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import providers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import com.actionfps.gameparser.enrichers.Implicits._
import com.actionfps.formats.json.Formats._
import views.rendergame.MixedGame

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

  private val processFn: JsonGame => Unit = processGame
  gamesProvider.addHook(processFn)
  applicationLifecycle.addStopHook(() => Future(gamesProvider.removeHook(processFn)))

  private val (newGamesEnum, thing) = Concurrent.broadcast[Event]
  val newGamesSource = Source.fromPublisher(Streams.enumeratorToPublisher(newGamesEnum))
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


}
