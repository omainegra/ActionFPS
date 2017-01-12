package controllers

import javax.inject._

import com.actionfps.clans.Conclusion.Namer
import lib.Clanner
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider
import providers.games.NewGamesProvider
import services.NewsService
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                newsService: NewsService,
                                newGamesProvider: NewGamesProvider,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider,
                                ladderController: LadderController)
                               (implicit executionContext: ExecutionContext) extends Controller {

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
      val headingO = await(newsService.latestItem().map(Option.apply).recover { case e =>
        Logger.error(s"Heading: ${e}", e)
        None
      })

      val cstats = await(fullProvider.clanstats).onlyRanked.named
      Ok(renderTemplate(None, supportsJson = false, None, wide = true)(
        views.html.index(
          games = games,
          events = events,
          latestClanwars = latestClanwars,
          bulletin = headingO,
          ladder = ladderController.aggregate.top(10),
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

}
