package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import com.actionfps.clans.{ClanNamer, CompleteClanwar}
import lib.KeepAliveEvents
import play.api.libs.EventSource.Event
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import providers.ReferenceProvider
import providers.games.NewGamesProvider
import services.ChallongeService.NewClanwarCompleted
import services.PingerService

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import EventStreamController._

@Singleton
class EventStreamController @Inject()(pingerService: PingerService,
                                      referenceProvider: ReferenceProvider,
                                      components: ControllerComponents)(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext)
    extends AbstractController(components) {

  private def namerF: Future[ClanNamer] = async {
    val clans = await(referenceProvider.clans)
    ClanNamer(id => clans.find(_.id == id).map(_.name))
  }

  private val clanwarsSource = {
    namerF.map { implicit namer =>
      Source
        .actorRef[NewClanwarCompleted](10, OverflowStrategy.dropBuffer)
        .mapMaterializedValue(
          actorSystem.eventStream.subscribe(_, classOf[NewClanwarCompleted]))
        .map(_.clanwarCompleted)
        .map(_.toEvent)
    }
  }

  def eventStream: Action[AnyContent] = Action.async {
    async {
      Ok.chunked(
        content = {
          pingerService.liveGamesWithRetainedSource
            .merge(NewGamesProvider.newGamesSource)
            .merge(await(clanwarsSource))
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
        content = NewGamesProvider.newGamesSource.merge(KeepAliveEvents.source)
      )
      .as("text/event-stream")
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
