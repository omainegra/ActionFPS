package controllers

import javax.inject._

import akka.stream.scaladsl.Source
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.clans._
import com.actionfps.clans.Conclusion.Namer
import lib.Clanner
import org.apache.commons.csv.{CSVFormat, CSVPrinter}
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsString, Json}
import play.api.libs.streams.Streams
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.filters.gzip.{Gzip, GzipFilter}
import providers.full.FullProvider
import providers.games.NewGamesProvider
import providers.ReferenceProvider
import services.PingerService
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                newGamesProvider: NewGamesProvider,
                                pingerService: PingerService,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider,
                                gzipFilter: GzipFilter)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext,
                                wSClient: WSClient) extends Controller {

  import common._

  import Clanwar.ImplicitFormats._

  def index = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      implicit val clanner = {
        val clans = await(referenceProvider.clans)
        Clanner(id => clans.find(_.id == id))
      }
      val games = await(fullProvider.getRecent).map(MixedGame.fromJsonGame)
      val events = await(fullProvider.events)
      val latestClanwar = await(fullProvider.clanwars).complete.toList.sortBy(_.id).lastOption.map(_.meta.named)
      val headingO = await(referenceProvider.bulletin)
      implicit val fmt = {
        implicit val d = Json.writes[ClanwarPlayer]
        implicit val c = Json.writes[ClanwarTeam]
        implicit val b = Json.writes[Conclusion]
        implicit val a = Json.writes[ClanwarMeta]
        Json.writes[IndexContents]
      }
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(IndexContents(games.map(_.game), events, latestClanwar)))
      else
        Ok(renderTemplate(None, supportsJson = true, None)(
          views.html.index(
            games = games,
            events = events,
            latestClanwar = latestClanwar,
            bulletin = headingO
          )))
    }
  }

  case class IndexContents(recentGames: List[JsonGame],
                           recentEvents: List[Map[String, String]],
                           latestClanwr: Option[ClanwarMeta]
                          )

  def game(id: String) = Action.async { implicit request =>
    async {
      await(fullProvider.game(id)) match {
        case Some(game) =>
          if (request.getQueryString("format").contains("json"))
            Ok(Json.toJson(game))
          else
            Ok(renderTemplate(None, supportsJson = true, None)(views.html.game(game)))
        case None => NotFound("Game not found")
      }
    }
  }

  def serverUpdates = Action {
    Ok.chunked(
      content = {
        Source(iterable = pingerService.status.get().valuesIterator.toList)
          .concat(pingerService.liveGamesSource)
      }
    ).as("text/event-stream")
  }

  def newGames = Action {
    Ok.chunked(
      content = newGamesProvider.newGamesSource
    ).as("text/event-stream")
  }

  def allTsv = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => s"${game.id}\t${game.toJson}\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/tab-separated-values")
        .withHeaders("Content-Disposition" -> "attachment; filename=games.tsv")
    }
  }

  def allCsv = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => CSVFormat.DEFAULT.format(game.id, game.toJson) + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/csv")
    }
  }

  def allTxt = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => game.toJson.toString() + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/plain")
    }
  }


  def allJson = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enum = allGames match {
        case head :: rest =>
          Enumerator(s"[\n  ${head.toJson}").andThen {
            Enumerator.enumerate(rest).map(game => ",\n  " + game.toJson.toString())
          }.andThen(Enumerator("\n]"))
        case Nil => Enumerator("[]")
      }
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enum)))
        .as("application/json")
    }
  }

}