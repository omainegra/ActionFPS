package services

import javax.inject.{Inject, Singleton}

import acleague.enrichers.JsonGame
import af.EnrichGames
import akka.actor.ActorSystem
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsBoolean, JsString, JsObject, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class NewGamesService @Inject()(val applicationLifecycle: ApplicationLifecycle,
                                val recordsService: RecordsService,
                                wSClient: WSClient,
                                gamesService: GamesService,
                                val validServersService: ValidServersService,
                                val configuration: Configuration)(implicit
                                                                  actorSystem: ActorSystem,
                                                                  executionContext: ExecutionContext)
  extends TailsGames {

  val (newGamesEnum, thing) = Concurrent.broadcast[Event]
  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))

  val logger = Logger(getClass)
  logger.info("Starting new games service")
  val url = configuration.underlying.getString("af.render.new-game")

  override def processGame(game: JsonGame): Unit = {
    val er = EnrichGames(recordsService.users, recordsService.clans)
    import er.withUsersClass
    val b = game.withoutHosts.withUsers.flattenPlayers.withClans.toJson.+("isNew" -> JsBoolean(true))
    wSClient.url(url).post(b).foreach {
      response =>
        thing.push(
          Event(
            id = Option(game.id),
            name = Option("new-game"),
            data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(response.body)).toString()
          )
        )
    }
  }

  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))

  initialiseTailer(fromStart = false)

}
