package controllers

import java.io.File
import javax.inject._

import lib.clans.{Clan, ResourceClans}
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

}