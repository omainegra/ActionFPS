package controllers

import javax.inject._

import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services._

import scala.concurrent.ExecutionContext


/**
  * This API depends on the games
  */
@Singleton
class Games @Inject()(gamesService: GamesService,
                     achievementsService: AchievementsService)
                     (implicit executionContext: ExecutionContext) extends Controller {

  def game(id: String) = Action {
    gamesService.allGames.get().find(_.id == id) match {
      case Some(game) => Ok(jsonToHtml("/game/", game.toJson))
      case None => NotFound("Game not found.")
    }
  }

}
