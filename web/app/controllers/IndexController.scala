package controllers

import java.time.Instant

import com.actionfps.clans.ClanNamer
import controllers.IndexController._
import lib.{Clanner, WebTemplateRender}
import play.api.Logger
import play.api.mvc._
import providers.{GameAxisAccumulatorInAgentFuture, ReferenceProvider}
import services.NewsService
import views.clanwar.Clanwar.ClanIdToClan
import views.ladder.Table.PlayerNamer
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class IndexController(webTemplateRender: WebTemplateRender,
                      newsService: NewsService,
                      referenceProvider: ReferenceProvider,
                      fullProvider: GameAxisAccumulatorInAgentFuture,
                      ladderController: LadderController,
                      components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components) {

  import webTemplateRender._

  private def namerF: Future[ClanNamer] = async {
    val clans = await(referenceProvider.clans)
    ClanNamer(id => clans.find(_.id == id).map(_.name))
  }

  private def clannerF: Future[Clanner] = async {
    val clans = await(referenceProvider.clans)
    Clanner(id => clans.find(_.id == id))
  }

  def index: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val playerNamer: PlayerNamer = PlayerNamer.fromMap(
        await(referenceProvider.Users.users)
          .map(u => u.id -> u.nickname.nickname)
          .toMap)
      val ladderAggregateF = ladderController.aggregate
      implicit val namer: ClanNamer = await(namerF)
      implicit val clanner: Clanner = await(clannerF)
      implicit val clanIdToClan: ClanIdToClan = ClanIdToClan(clanner.get)
      val games = await(fullProvider.getRecent(NumberOfRecentGames))
        .map(MixedGame.fromJsonGame)
      val events = await(fullProvider.events)
      val latestClanwars = await(fullProvider.clanwars).complete.toList
        .sortBy(_.id)
        .reverse
        .take(NumberOfLatestClanwars)
        .map(_.meta.named)
      val headingO =
        await(newsService.latestItemFuture().map(Option.apply).recover {
          case e =>
            Logger.error(s"Heading: ${e}", e)
            None
        })

      val cstats = await(fullProvider.clanstats)
        .shiftedElo(Instant.now())
        .onlyRanked
        .named
      Ok(
        renderTemplate(
          title = Some("ActionFPS First Person Shooter"),
          sourceLink = false,
          wide = true
        )(views.html.index(
          games = games,
          events = events,
          latestClanwars = latestClanwars,
          bulletin = headingO,
          ladder = await(ladderAggregateF).top(NumberOfLadderPlayers),
          playersStats = await(fullProvider.playerRanks).onlyRanked
            .top(NumberOfPlayerRanks),
          clanStats = cstats.top(NumberOfClanRanks)
        )))
    }
  }

}

object IndexController {
  val NumberOfRecentGames = 10
  val NumberOfLatestClanwars = 10
  val NumberOfLadderPlayers = 10
  val NumberOfPlayerRanks = 10
  val NumberOfClanRanks = 10
}
