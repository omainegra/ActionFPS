package controllers

import javax.inject._

import akka.stream.scaladsl.Source
import org.apache.commons.csv.CSVFormat
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.libs.streams.Streams
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import providers.full.FullProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class AllGames @Inject()(fullProvider: FullProvider)
                        (implicit executionContext: ExecutionContext) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/") | GET(p"/games.tsv") => allTsv
    case GET(p"/games.ndjson") => allNdJson
    case GET(p"/games.json") => allJson
    case GET(p"/games.csv") => allCsv
  }

  private def allTsv: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => s"${game.id}\t${Json.toJson(game)}\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/tab-separated-values")
        .withHeaders("Content-Disposition" -> "attachment; filename=games.tsv")
    }
  }

  private def allCsv: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => CSVFormat.DEFAULT.format(game.id, Json.toJson(game)) + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/csv")
    }
  }

  private def allNdJson: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => Json.toJson(game).toString() + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/plain")
    }
  }

  private def allJson: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enum = allGames match {
        case head :: rest =>
          Enumerator(s"[\n  ${Json.toJson(head)}").andThen {
            Enumerator.enumerate(rest).map(game => ",\n  " + Json.toJson(game).toString())
          }.andThen(Enumerator("\n]"))
        case Nil => Enumerator("[]")
      }
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enum)))
        .as("application/json")
    }
  }

}
