package controllers

import javax.inject._

import akka.stream.scaladsl.Source
import com.actionfps.clans.Conclusion.Namer
import lib.Clanner
import org.apache.commons.csv.CSVFormat
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.libs.streams.Streams
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import play.filters.gzip.GzipFilter
import providers.ReferenceProvider
import providers.full.FullProvider
import providers.games.NewGamesProvider
import services.PingerService
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                newGamesProvider: NewGamesProvider,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider,
                                ladderController: LadderController,
                                gzipFilter: GzipFilter)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext,
                                wSClient: WSClient) extends Controller {

  import common._

  def recentGames: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      val games = await(fullProvider.getRecent(100)).map(MixedGame.fromJsonGame)
      Ok(renderTemplate(None, supportsJson = false, None, wide = false)(
        views.html.recent_games(games)
      ))
    }
  }

  def index: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      implicit val clanner = {
        val clans = await(referenceProvider.clans)
        Clanner(id => clans.find(_.id == id))
      }
      val games = await(fullProvider.getRecent(10)).map(MixedGame.fromJsonGame)
      val events = await(fullProvider.events)
      val latestClanwars = await(fullProvider.clanwars).complete.toList.sortBy(_.id).reverse.take(10).map(_.meta.named)
      val headingO = await(referenceProvider.bulletin)

      val cstats = await(fullProvider.clanstats).onlyRanked.named
      Ok(renderTemplate(None, supportsJson = false, None, wide = true)(
        views.html.index(
          games = games,
          events = events,
          latestClanwars = latestClanwars,
          bulletin = headingO,
          ladder = ladderController.agg.get().top(10),
          playersStats = await(fullProvider.playerRanks).onlyRanked.top(10),
          clanStats = cstats.top(10)
        )))
    }
  }

  def game(id: String): Action[AnyContent] = Action.async { implicit request =>
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


  def newGames = Action {
    Ok.chunked(
      content = newGamesProvider.newGamesSource
    ).as("text/event-stream")
  }

  def allTsv: Action[AnyContent] = Action.async {
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

  def allCsv: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => CSVFormat.DEFAULT.format(game.id, Json.toJson(game)) + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/csv")
    }
  }

  def allTxt: Action[AnyContent] = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => Json.toJson(game).toString() + "\n")
      Ok.chunked(Source.fromPublisher(Streams.enumeratorToPublisher(enumerator)))
        .as("text/plain")
    }
  }


  def allJson: Action[AnyContent] = Action.async {
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
