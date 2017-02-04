package controllers

import java.time.Instant
import javax.inject._

import com.actionfps.clans.Conclusion.Namer
import controllers.LadderController.PlayerNamer
import lib.{Clanner, WebTemplateRender}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider
import providers.games.NewGamesProvider
import services.NewsService
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(webTemplateRender: WebTemplateRender,
                                newsService: NewsService,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider,
                                ladderController: LadderController)
                               (implicit executionContext: ExecutionContext) extends Controller {

  import webTemplateRender._

  private def namerF: Future[Namer] = async {
    val clans = await(referenceProvider.clans)
    Namer(id => clans.find(_.id == id).map(_.name))
  }

  private def clannerF: Future[Clanner] = async {
    val clans = await(referenceProvider.clans)
    Clanner(id => clans.find(_.id == id))
  }

  def recentGames: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = await(namerF)
      val games = await(fullProvider.getRecent(100)).map(MixedGame.fromJsonGame)
      Ok(renderTemplate(title = Some("Recent Games"), supportsJson = false)(
        views.html.recent_games(games)
      ))
    }
  }

  def index: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val playerNamer = PlayerNamer.fromMap(await(referenceProvider.Users().users).map(u => u.id -> u.nickname.nickname).toMap)
      implicit val namer = await(namerF)
      implicit val clanner = await(clannerF)
      val games = await(fullProvider.getRecent(10)).map(MixedGame.fromJsonGame)
      val events = await(fullProvider.events)
      val latestClanwars = await(fullProvider.clanwars).complete.toList.sortBy(_.id).reverse.take(10).map(_.meta.named)
      val headingO = await(newsService.latestItemFuture().map(Option.apply).recover { case e =>
        Logger.error(s"Heading: ${e}", e)
        None
      })

      val cstats = await(fullProvider.clanstats).shiftedElo(Instant.now()).onlyRanked.named
      Ok(renderTemplate(
        title = None,
        supportsJson = false,
        wide = true
      )(
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

}
