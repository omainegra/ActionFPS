package providers
package games

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.JsonGame
import lib.WebTemplateRender
import play.api.libs.EventSource.Event
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import providers.full.NewGameDetected
import views.rendergame.MixedGame

import scala.concurrent.ExecutionContext

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class NewGamesProvider @Inject()(implicit actorSystem: ActorSystem,
                                 executionContext: ExecutionContext) {

  val newGamesSource: Source[Event, Boolean] = {
    Source
      .actorRef[NewGameDetected](10, OverflowStrategy.dropHead)
      .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[NewGameDetected]))
      .map(_.jsonGame)
      .map(NewGamesProvider.gameToEvent)
  }

}

object NewGamesProvider {
  def gameToEvent(game: JsonGame): Event = {
    val b = Json.toJson(game.withoutHosts).asInstanceOf[JsObject].+("isNew" -> JsBoolean(true))

    val gameHtml = views.rendergame.Render.renderMixedGame(MixedGame.fromJsonGame(game))
    Event(
      id = Option(game.id),
      name = Option("new-game"),
      data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(gameHtml.body)).toString()
    )
  }
}
