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
                        achievementsService: AchievementsService,
                        phpRenderService: PhpRenderService
                       )
                       (implicit executionContext: ExecutionContext) extends Controller {
import scala.async.Async._
  def players = Action.async { implicit req =>
    async {
      await(phpRenderService("/players/", Json.toJson(recordsService.users.map(_.toJson))))
    }
  }

  def player(id: String) = Action.async { implicit req =>
    async {
      recordsService.users.find(user => user.id == id || user.email == id) match {
        case Some(user) =>
          val json = achievementsService.achievements.get().map.get(user.id) match {
            case Some(playerState) =>
              fullProfile(user, playerState)
            case None =>
              Json.toJson(user)
          }
          await(phpRenderService("/player/", json))
        case None =>
          NotFound("User not found")
      }
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
