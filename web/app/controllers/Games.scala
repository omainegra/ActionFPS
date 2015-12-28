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
import scala.async.Async._

/**
  * This API depends on the games
  */
@Singleton
class Games @Inject()(gamesService: GamesService,
                      achievementsService: AchievementsService,
                      phpRenderService: PhpRenderService)
                     (implicit executionContext: ExecutionContext) extends Controller {


  def game(id: String) = Action.async { implicit req =>
    async {
      gamesService.allGames.get().find(_.id == id) match {
        case Some(game) =>
          await(phpRenderService("/game/", game.toJson))
        case None =>
          NotFound("Game not found.")
      }
    }
  }

}
