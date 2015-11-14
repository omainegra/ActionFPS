package controllers

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.{Jsons, PlayerState}
import acleague.ranker.achievements.immutable.NotAchievedAchievements$
import lib.clans.{Clan, ResourceClans}
import lib.users.{User, BasexUsers}
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext


@Singleton
class ApiMain @Inject()(configuration: Configuration)
                       (implicit executionContext: ExecutionContext) extends Controller {

  val file = new File(configuration.underlying.getString("af.games.path"))

  val allLines = scala.io.Source.fromFile(file).getLines.toList
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

  def cevs = allLines.map(_.split("\t").toList).foldLeft((Map.empty[String, PlayerState], List.empty[String])) {
    case ((combined, sofar), List(_, "GOOD", _, json)) =>
      val jsonGame = JsonGame.fromJson(json)
      val oEvents = scala.collection.mutable.Buffer.empty[String]
      var nComb = combined
      for {
        team <- jsonGame.teams
        player <- team.players
        user <- BasexUsers.users.find(_.nickname.nickname == player.name)
        (newPs, newEvents) <- combined.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p => BasexUsers.users.exists(_.nickname.nickname == p.name))
      } {
        oEvents ++= newEvents.map{s => s"${s._1} ${user.name} ${s._2}"}
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