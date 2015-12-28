package services

import javax.inject.{Inject, Singleton}

import acleague.enrichers.JsonGame
import akka.actor.ActorSystem
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsBoolean, JsObject, Json}
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class NewGamesService @Inject()(val applicationLifecycle: ApplicationLifecycle,
                                val configuration: Configuration,
                                eventPublisherService: EventPublisherService)
                               (implicit
                                actorSystem: ActorSystem,
                                executionContext: ExecutionContext)
  extends TailsGames {

  val logger = Logger(getClass)
  logger.info("Starting new games service")

  override def processGame(game: JsonGame): Unit = {
    val b = game.withoutHosts.toJson.+("isNew" -> JsBoolean(true))
    eventPublisherService.push(
      Event(
        id = Option(game.id),
        name = Option("new-game"),
        data = Json.toJson(b).asInstanceOf[JsObject].toString()
      )
    )
  }

  initialiseTailer(fromStart = false)

}
