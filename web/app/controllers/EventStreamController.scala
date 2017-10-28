package controllers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.actionfps.clans.{ClanNamer, CompleteClanwar}
import com.actionfps.gameparser.enrichers.JsonGame
import controllers.EventStreamController._
import lib.KeepAliveEvents
import play.api.libs.EventSource.Event
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import providers.ReferenceProvider
import providers.games.NewGamesProvider
import services.PingerService

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Serve various EventSource streams.
  */
class EventStreamController(
    clanwarsSource: Source[CompleteClanwar, Future[NotUsed]],
    newGamesSource: Source[JsonGame, Future[NotUsed]],
    pingerService: PingerService,
    referenceProvider: ReferenceProvider,
    components: ControllerComponents)(implicit actorSystem: ActorSystem,
                                      executionContext: ExecutionContext)
    extends AbstractController(components) {

  private def namerF: Future[ClanNamer] = async {
    val clans = await(referenceProvider.clans)
    ClanNamer(id => clans.find(_.id == id).map(_.name))
  }

  private val clanwarsEventSource = {
    namerF.map { implicit namer =>
      clanwarsSource
        .map(_.toEvent)
    }
  }

  def eventStream: Action[AnyContent] = Action.async {
    async {
      Ok.chunked(
        content = {
          pingerService.liveGamesWithRetainedSource
            .merge(newGamesSource.map(NewGamesProvider.gameToEvent))
            .merge(await(clanwarsEventSource))
            .merge(KeepAliveEvents.source)
        }
      )
    }
  }

  def serverUpdates = Action {
    Ok.chunked(
        content = pingerService.liveGamesWithRetainedSource.merge(
          KeepAliveEvents.source)
      )
      .as("text/event-stream")
  }

  def newGames = Action {
    Ok.chunked(
        content = newGamesSource
          .map(NewGamesProvider.gameToEvent)
          .merge(KeepAliveEvents.source)
      )
      .as("text/event-stream")
  }

  def newClanwars = Action.async {
    async {
      Ok.chunked(
          content = newGamesSource
            .map(NewGamesProvider.gameToEvent)
            .merge(await(clanwarsEventSource))
            .merge(KeepAliveEvents.source)
        )
        .as("text/event-stream")
    }
  }

}

object EventStreamController {
  implicit class RichCompleteClanwar(completeClanwar: CompleteClanwar) {
    def toEvent(implicit writes: Writes[CompleteClanwar]): Event = {
      Event(
        data = Json.toJson(completeClanwar).toString,
        name = Some("clanwar"),
        id = Some(completeClanwar.id)
      )
    }
  }
}
