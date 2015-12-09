package controllers

import javax.inject._

import acleague.ranker.achievements.{PlayerState, Jsons}
import acleague.ranker.achievements.immutable.PlayerStatistics
import af.rr.ServerRecord
import lib.clans.Clan
import lib.users.User
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext


@Singleton
class ApiMain @Inject()(configuration: Configuration,
                        gamesService: GamesService,
                        recordsService: RecordsService,
                        pingerService: PingerService,
                        intersService: IntersService,
                        achievementsService: AchievementsService)
                       (implicit executionContext: ExecutionContext) extends Controller {

  def recent = Action {
    Ok(JsArray(gamesService.allGames.get().takeRight(30).reverse.map(_.toJson)))
  }

  def recentClangames = Action {
    Ok(JsArray(gamesService.allGames.get().filter(_.clangame.isDefined).takeRight(30).reverse.map(_.toJson)))
  }

  implicit val serversWrites = Json.writes[ServerRecord]

  def getServers = Action {
    Ok(Json.toJson(recordsService.servers))
  }

  def usersJson = Action {
    import User.WithoutEmailFormat.noEmailUserWrite
    Ok(Json.toJson(recordsService.users))
  }

  def userJson(id: String) = Action {
    recordsService.users.find(user => user.id == id || user.email == id) match {
      case Some(user) =>
        import User.WithoutEmailFormat.noEmailUserWrite
        Ok(Json.toJson(user))
      case None =>
        NotFound("User not found")
    }
  }

  implicit val fmtClan = Json.format[Clan]

  def clans = Action {
    Ok(Json.toJson(recordsService.clans))
  }

  def game(id: String) = Action {
    gamesService.allGames.get().find(_.id == id) match {
      case Some(game) => Ok(game.toJson)
      case None => NotFound("Game not found")
    }
  }

  def all = Action {
    val enumerator = Enumerator
      .enumerate(gamesService.allGames.get())
      .map(game => s"${game.id}\t${game.toJson}\n")
    Ok.chunked(enumerator).as("text/tab-separated-values")
  }

  def listEvents = Action {
    Ok(Json.toJson(achievementsService.achievements.get().events.take(10)))
  }

  def fullProfile(user: User, playerState: PlayerState) = {
    import Jsons._
    import PlayerStatistics.fmts
    import User.WithoutEmailFormat.noEmailUserWrite
    Json.toJson(user).asInstanceOf[JsObject].deepMerge(
      JsObject(
        Map(
          "stats" -> Json.toJson(playerState.playerStatistics),
          "achievements" -> Json.toJson(playerState.buildAchievements)
        )
      )
    )
  }

  def fullUser(id: String) = Action {
    val fullOption = for {
      user <- recordsService.users.find(user => user.id == id || user.email == id)
      playerState <- achievementsService.achievements.get().map.get(user.id)
    } yield fullProfile(user, playerState)
    fullOption match {
      case Some(json) => Ok(json)
      case None => NotFound("User not found")
    }
  }

  def usersFull = Action {
    val theMap = {
      for {
        user <- recordsService.users
        playerState <- achievementsService.achievements.get().map.get(user.id)
      } yield user.id -> fullProfile(user, playerState)
    }.toMap
    Ok(Json.toJson(theMap))
  }

  def achievements(id: String) = Action {
    achievementsService.achievements.get().map.get(id) match {
      case None => NotFound("Player id not found")
      case Some(player) =>
        import Jsons._
        Ok(Json.toJson(player.buildAchievements))
    }
  }

  def listNicknames() = Action { request =>
    val names = gamesService.allGames.get().flatMap(_.teams.flatMap(_.players)).map(_.name)
    val nameToCount = names.groupBy(identity).mapValues(_.length).toList.sortBy(_._2).reverse
    if (request.queryString.get("with").exists(_.contains("game-counts"))) {
      Ok(Json.toJson(nameToCount.map { case (name, count) =>
        JsObject(Map("name" -> JsString(name), "games" -> JsNumber(count))
        )
      }))
    } else {
      Ok(Json.toJson(nameToCount.map { case (name, count) => name }))
    }
  }

  def serverUpdates = Action {
    Ok.feed(
      content = pingerService.liveGamesEnum
    ).as("text/event-stream")
  }

  def inters = Action {
    Ok.feed(
      content = intersService.intersEnum
    ).as("text/event-stream")
  }

}
