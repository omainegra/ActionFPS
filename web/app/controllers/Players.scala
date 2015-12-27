package controllers

import javax.inject._

import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import af.User
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext


/**
  * This API depends on the games
  */
@Singleton
class Players @Inject()(configuration: Configuration,
                        gamesService: GamesService,
                        recordsService: RecordsService,
                        achievementsService: AchievementsService
                       )
                       (implicit executionContext: ExecutionContext) extends Controller {

  def players =  Action {
    Ok(jsonToHtml("/players/", Json.toJson(recordsService.users.map(_.toJson))))
  }

  def player(id: String) = Action {
    val fullOption = for {
      user <- recordsService.users.find(user => user.id == id || user.email == id)
      playerState <- achievementsService.achievements.get().map.get(user.id)
    } yield fullProfile(user, playerState)
    fullOption match {
      case Some(json) => Ok(jsonToHtml("/player/", json))
      case None => NotFound("User not found")
    }
  }

  def fullProfile(user: User, playerState: PlayerState) = {
    import Jsons._
    import PlayerStatistics.fmts
    import User.WithoutEmailFormat.noEmailUserWrite
    Json.toJson(user).asInstanceOf[JsObject].deepMerge(
      JsObject(
        Map(
          "stats" -> Json.toJson(playerState.playerStatistics),
          "achievements" -> Json.toJson(playerState.buildAchievements),
          "recent-games" ->
            Json.toJson(playerState.playerStatistics.playedGames.sorted.takeRight(7).reverse.flatMap(gid =>
              gamesService.allGames.get().find(_.id == gid).map(_.toJson)
            ))
        )
      )
    )
  }

}
