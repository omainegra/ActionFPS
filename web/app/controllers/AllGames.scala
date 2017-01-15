package controllers

import javax.inject._

import akka.stream.scaladsl.Source
import org.apache.commons.csv.CSVFormat
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import providers.full.FullProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Provide a batch list of all games in different formats.
  */
@Singleton
class AllGames @Inject()(fullProvider: FullProvider)
                        (implicit executionContext: ExecutionContext) extends SimpleRouter {

  import fullProvider.allGames

  override def routes: Routes = {
    case GET(p"/") | GET(p"/games.tsv") => allTsv
    case GET(p"/games.ndjson") => allNdJson
    case GET(p"/games.json") => allJson
    case GET(p"/games.csv") => allCsv
  }

  private def allTsv: Action[AnyContent] = Action.async {
    async {
      Ok.chunked(
        Source(await(allGames))
          .map(game => s"${game.id}\t${Json.toJson(game)}\n")
      )
        .as("text/tab-separated-values")
        .withHeaders("Content-Disposition" -> "attachment; filename=games.tsv")
    }
  }

  private def allCsv: Action[AnyContent] = Action.async {
    async {
      Ok.chunked(Source(await(allGames)).map(game => CSVFormat.DEFAULT.format(game.id, Json.toJson(game)) + "\n"))
        .as("text/csv")
    }
  }

  private def allNdJson: Action[AnyContent] = Action.async {
    async {
      Ok.chunked(Source(await(allGames)).map(game => Json.toJson(game).toString() + "\n"))
        .as("text/plain")
    }
  }

  private def allJson: Action[AnyContent] = Action.async {
    async {
      val source = await(allGames) match {
        case head :: rest =>
          Source(List("[\n", Json.toJson(head).toString))
            .concat(Source(rest).map(game => ",\n  " + Json.toJson(game).toString()))
            .concat(Source(List("\n]")))
        case Nil => Source(List("[]"))
      }
      Ok.chunked(source)
        .as("application/json")
    }
  }

}
