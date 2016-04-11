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
import play.api.libs.json.{JsBoolean, JsString, JsObject, Json}
import play.api.libs.streams.Streams
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import providers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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

  val processFn: JsonGame => Unit = processGame
  gamesProvider.addHook(processFn)
  applicationLifecycle.addStopHook(() => Future(gamesProvider.removeHook(processFn)))

  val (newGamesEnum, thing) = Concurrent.broadcast[Event]
  val newGamesSource = Source.fromPublisher(Streams.enumeratorToPublisher(newGamesEnum))
  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))

  val logger = Logger(getClass)
  logger.info("Starting new games provider")

  def processGame(game: JsonGame): Unit = {
    //    val er = EnrichGames(recordsService.users, recordsService.com.actionfps.clans)
    //    import er.withUsersClass
    //    val b = game.withoutHosts.withUsers.flattenPlayers.withClans.toJson.+("isNew" -> JsBoolean(true))
    val b = game.withoutHosts.toJson.+("isNew" -> JsBoolean(true))

    val jsonMap = Map("game" -> Seq(b.toString()), "maps" -> Seq(Json.toJson(Maps.resource.maps.mapValues(_.image)).toString()))
    common.renderRaw("/live/render-fragment.php")(_.post(jsonMap)).foreach(response =>
      thing.push(
        Event(
          id = Option(game.id),
          name = Option("new-game"),
          data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(response.body)).toString()
        )
      )
    )
  }


}
