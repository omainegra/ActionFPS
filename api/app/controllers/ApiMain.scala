package controllers

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.{Jsons, PlayerState}
import acleague.ranker.achievements.immutable.{PlayerStatistics, NotAchievedAchievements$}
import lib.clans.{Clan, ResourceClans}
import lib.users.{User, BasexUsers}
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, JsArray, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext


@Singleton
class ApiMain @Inject()(configuration: Configuration)
                       (implicit executionContext: ExecutionContext) extends Controller {

  val file = new File(configuration.underlying.getString("af.games.path"))

  val allLines = scala.io.Source.fromFile(file).getLines.toList //.take(500)
  val allReverseLines = allLines.reverse

  def recentGames = allReverseLines.toIterator.map(_.split("\t").toList).collect {
    case List(id, "GOOD", "", json) => id -> Json.parse(json)
  }.take(10).toList

  val lines = scala.io.Source.fromFile(file).getLines.map(_.split("\t").toList).collect {
    case List(id, "GOOD", _, json) => s"$id\t$json  "
  }.take(10).toList

  def recent = Action {
    Ok(JsArray(recentGames.map { case (_, json) => json }))
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
    Ok.chunked(Enumerator.enumerate(lines).map(l => s"$l\n")).as("text/tab-separated-values")
  }

  def cevs = allLines.map(_.split("\t").toList).foldLeft((Map.empty[String, PlayerState], List.empty[Map[String, String]])) {
    case ((combined, sofar), List(_, "GOOD", _, json)) =>
      val jsonGame = JsonGame.fromJson(json)
      val oEvents = scala.collection.mutable.Buffer.empty[Map[String, String]]
      var nComb = combined
      for {
        team <- jsonGame.teams
        player <- team.players
        user <- BasexUsers.users.find(_.nickname.nickname == player.name)
        (newPs, newEvents) <- combined.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p => BasexUsers.users.exists(_.nickname.nickname == p.name))
      } {
        oEvents ++= newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
        nComb = nComb.updated(user.id, newPs)
      }
      (nComb, oEvents.toList ++ sofar)
    case (x, List(_, "BAD", _, _)) =>
      x
  }

  lazy val cavs = cevs

  def listEvents = Action {
    //    Ok(s"$combs")
    cavs match {
      case (ss, events) =>
        Ok(Json.toJson(events.take(10)))
      //        ss("drakas").achieved.foreach(println)
      //        ss("drakas").combined.combined.foreach(println)
      //        import Jsons._
      //        Ok(Json.toJson(ss("drakas").buildAchievements))
      //        Ok(Json.toJson(events))
    }
  }

  def fullUser(id: String) = Action {
    val fullOption = for {
      user <- BasexUsers.users.find(_.id == id)
      playerState <- cavs._1.get(user.id)
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
    cavs match {
      case (ss, _) =>
        ss.get(id) match {
          case None => NotFound("Player id not found")
          case Some(player) =>
            import Jsons._
            Ok(Json.toJson(player.buildAchievements))
        }
    }
  }

}