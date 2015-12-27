package controllers

/**
  * Created by William on 27/12/2015.
  */
import javax.inject._

import play.api.libs.json.{Json, JsArray}
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services.{AchievementsService, GamesService, GameRenderService}

@Singleton
class Website @Inject()(gameRenderService: GameRenderService,
                        gamesService: GamesService,
                        achievementsService: AchievementsService
                       ) extends Controller {

  def index = Action {
    def recent = JsArray(gamesService.allGames.get().takeRight(50).reverse.map(_.toJson))
    val r = gameRenderService.query(
      path = "/index.php",
      data = Map(
        "/recent/" -> Json.toJson(recent).toString(),
        "/events/" ->  Json.toJson(achievementsService.achievements.get().events.take(10)).toString()
      )
    )
    Ok(Html(r))
  }
}
