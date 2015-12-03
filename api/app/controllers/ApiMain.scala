package controllers

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import lib.clans.{Clan, ResourceClans}
import lib.users.{User, BasexUsers}
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, JsArray, Json}
import play.api.mvc.{Action, Controller}
import services.{AchievementsService, GamesService}

import scala.concurrent.ExecutionContext


@Singleton
class ApiMain @Inject()(configuration: Configuration,
                        gamesService: GamesService,
                        achievementsService: AchievementsService)
                       (implicit executionContext: ExecutionContext) extends Controller {

  def recent = Action {
    Ok(JsArray(gamesService.allGames.get().takeRight(17).reverse.map(_.toJson)))
  }

  def usersJson = Action {
    import User.WithoutEmailFormat.noEmailUserWrite
    Ok(Json.toJson(BasexUsers.users))
  }

  def userJson(id: String) = Action {
    BasexUsers.users.find(_.id == id) match {
      case Some(user) =>
        import User.WithoutEmailFormat.noEmailUserWrite
        Ok(Json.toJson(user))
      case None =>
        NotFound("User not found")
    }
  }

  implicit val fmtClan = Json.format[Clan]

  def clansJson = Action {

    Ok(Json.toJson(ResourceClans.clans))
  }

  def clansYaml = Action {
    Ok(ResourceClans.yaml).as("text/x-yaml; charset=utf-8")
  }

  def raw = Action {
    val enumerator = Enumerator
      .enumerate(gamesService.allGames.get())
      .map(game => s"${game.id}\t${game.toJson}\n")
    Ok.chunked(enumerator).as("text/tab-separated-values")
  }

  def listEvents = Action {
    Ok(Json.toJson(achievementsService.achievements.get().events.take(10)))
  }

  def fullUser(id: String) = Action {
    val fullOption = for {
      user <- BasexUsers.users.find(_.id == id)
      playerState <- achievementsService.achievements.get().map.get(user.id)
    } yield {
      import User.WithoutEmailFormat.noEmailUserWrite
      import Jsons._
      import PlayerStatistics.fmts
      Json.toJson(user).asInstanceOf[JsObject].deepMerge(
        JsObject(
          Map(
            "stats" -> Json.toJson(playerState.playerStatistics),
            "achievements" -> Json.toJson(playerState.buildAchievements)
          )
        )
      )
    }
    fullOption match {
      case Some(json) => Ok(json)
      case None => NotFound("User not found")
    }
  }

  def achievements(id: String) = Action {
    achievementsService.achievements.get().map.get(id) match {
      case None => NotFound("Player id not found")
      case Some(player) =>
        import Jsons._
        Ok(Json.toJson(player.buildAchievements))
    }
  }

}
