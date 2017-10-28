package controllers

import akka.stream.scaladsl.Source
import com.actionfps.gameparser.enrichers.JsonGame
import org.apache.commons.csv.CSVFormat
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import scala.async.Async._
import scala.concurrent.ExecutionContext

import com.actionfps.formats.json.Formats._

/**
  * Provide a batch list of all games in different formats.
  */
class AllGames(providesGames: ProvidesGames, components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components)
    with SimpleRouter {

  import providesGames._

  override def routes: Routes = {
    case GET(p"/") | GET(p"/games.tsv") => allTsv
    case GET(p"/games.ndjson") => allNdJson
    case GET(p"/games.json") => allJson
    case GET(p"/games.csv") => allCsv
  }

  def gameFilter(implicit requestHeader: RequestHeader): JsonGame => Boolean = {
    requestHeader.getQueryString("since") match {
      case Some(id) => _.id >= id
      case None => Function.const(true)
    }
  }

  private def allTsv: Action[AnyContent] = Action.async { implicit req =>
    async {
      Ok.chunked(
          Source(await(allGames).filter(gameFilter))
            .map(game => s"${game.id}\t${Json.toJson(game)}\n")
        )
        .as("text/tab-separated-values")
        .withHeaders("Content-Disposition" -> "attachment; filename=games.tsv")
    }
  }

  private def allCsv: Action[AnyContent] = Action.async { implicit req =>
    async {
      Ok.chunked(Source(await(allGames).filter(gameFilter))
          .map(game =>
            CSVFormat.DEFAULT.format(game.id, Json.toJson(game)) + "\n"))
        .as("text/csv")
    }
  }

  private def allNdJson: Action[AnyContent] = Action.async { implicit req =>
    async {
      Ok.chunked(Source(await(allGames).filter(gameFilter))
          .map(game => Json.toJson(game).toString() + "\n"))
        .as("text/plain")
    }
  }

  private def allJson: Action[AnyContent] = Action.async { implicit req =>
    async {
      val source = await(allGames).filter(gameFilter) match {
        case head :: rest =>
          Source(List("[\n", Json.toJson(head).toString))
            .concat(Source(rest).map(game =>
              ",\n  " + Json.toJson(game).toString()))
            .concat(Source(List("\n]")))
        case Nil => Source(List("[]"))
      }
      Ok.chunked(source)
        .as("application/json")
    }
  }

}
