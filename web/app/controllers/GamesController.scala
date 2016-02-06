package controllers

import javax.inject._

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.clans._
import com.actionfps.clans.Conclusion.Namer
import lib.Clanner
import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsString, Json}
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
        Ok(renderTemplate(None, supportsJson = true, None)(views.html.index(games = games, events = events, latestClanwar = latestClanwar)))
    }
  }

  case class IndexContents(recentGames: List[JsonGame], recentEvents: List[Map[String, String]], latestClanwr: Option[ClanwarMeta])

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
    Ok.feed(
      content = Enumerator.enumerate(pingerService.status.get().valuesIterator).andThen(pingerService.liveGamesEnum)
    ).as("text/event-stream")
  }

  def newGames = Action {
    Ok.feed(
      content = newGamesProvider.newGamesEnum
    ).as("text/event-stream")
  }

  def all = Action.async {
    async {
      val allGames = await(fullProvider.allGames)
      val enumerator = Enumerator
        .enumerate(allGames)
        .map(game => s"${game.id}\t${game.toJson}\n")
      val gzEnum = enumerator.map(_.getBytes("UTF-8")).&>(Gzip.gzip(123))
      Ok.chunked(gzEnum)
        .as("text/tab-separated-values")
        .withHeaders("Content-Encoding" -> "gzip")
    }
  }

}