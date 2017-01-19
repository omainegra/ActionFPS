package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import com.actionfps.clans.Conclusion.Namer
import lib.KeepAliveEvents
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider
import providers.full.NewClanwarCompleted
import providers.games.NewGamesProvider
import services.{IntersService, PingerService}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

class EventStream @Inject()(pingerService: PingerService,
                            intersService: IntersService,
                            referenceProvider: ReferenceProvider,
                            newGamesProvider: NewGamesProvider)
                           (implicit actorSystem: ActorSystem,
                            executionContext: ExecutionContext) extends Controller {

  private def namerF: Future[Namer] = async {
    val clans = await(referenceProvider.clans)
    Namer(id => clans.find(_.id == id).map(_.name))
  }

  private val clanwarsSource = {
    namerF.map { implicit namer =>
      Source
        .actorRef[NewClanwarCompleted](10, OverflowStrategy.dropBuffer)
        .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[NewClanwarCompleted]))
        .map(_.toEvent)
    }
  }


  def eventStream() = Action.async {
    async {
      Ok.chunked(
        content = {
          pingerService
            .liveGamesSource
            .merge(newGamesProvider.newGamesSource)
            .merge(intersService.intersSource)
            .merge(await(clanwarsSource))
            .merge(KeepAliveEvents.source)
        }
      )
    }
  }

}
