package providers.games

import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.libs.EventSource.Event
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import views.rendergame.MixedGame

/**
  * Created by William on 09/12/2015.
  */
object NewGamesProvider {

  def gameToEvent(game: JsonGame): Event = {
    val b = Json
      .toJson(game.withoutHosts)
      .asInstanceOf[JsObject]
      .+("isNew" -> JsBoolean(true))

    val gameHtml =
      views.rendergame.Render.renderMixedGame(MixedGame.fromJsonGame(game))
    Event(
      id = Option(game.id),
      name = Option("new-game"),
      data = Json
        .toJson(b)
        .asInstanceOf[JsObject]
        .+("html" -> JsString(gameHtml.body))
        .toString()
    )
  }
}
