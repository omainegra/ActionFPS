package controllers

/**
  * Created by William on 01/01/2016.
  */
import java.time.Instant
import javax.inject._

import com.actionfps.accumulation.Clan
import com.actionfps.clans.{ClanNamer, Clanwar}
import com.actionfps.stats.Clanstat
import controllers.ClansController.ClanView
import lib.{ClanDataProvider, Clanner, ClansProvider, WebTemplateRender}
import play.api.Configuration
import play.api.libs.json.{Json, OWrites, Writes}
import play.api.mvc._
import views.ClanRankings
import views.clanwar.Clanwar.ClanIdToClan
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext
import com.actionfps.formats.json.Formats._

@Singleton
class ClansController @Inject()(webTemplateRender: WebTemplateRender,
                                clansProvider: ClansProvider,
                                clanDataProvider: ClanDataProvider,
                                components: ControllerComponents)(
    implicit configuration: Configuration,
    executionContext: ExecutionContext)
    extends AbstractController(components) {

  private def namerF = async {
    val clans = await(clansProvider.clans)
    ClanNamer(id => clans.find(_.id == id).map(_.name))
  }

  private def clannerF = async {
    val clans = await(clansProvider.clans)
    Clanner(id => clans.find(_.id == id))
  }

  private def clanIdToClan = async {
    val clans = await(clansProvider.clans)
    ClanIdToClan(id => clans.find(_.id == id))
  }

  def rankings: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer: ClanNamer {
        def clanName(id: String): Option[String]
      } = await(namerF)
      val stats = await(clanDataProvider.clanstats)
        .shiftedElo(Instant.now())
        .onlyRanked
        .named
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(stats))
      else
        Ok(
          webTemplateRender.renderTemplate(
            title = Some("Clan Rankings"),
            jsonLink = Some("?format=json")
          )(views.html.clan_ranks_wrap(ClanRankings.render(stats))))
    }
  }

  def clan(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer: ClanNamer {
        def clanName(id: String): Option[String]
      } = await(namerF)

      val ccw = await(clanDataProvider.clanwars).all
        .filter(_.clans.contains(id))
        .toList
        .sortBy(_.id)
        .reverse
        .take(15)

      val st = await(clanDataProvider.clanstats)
        .shiftedElo(Instant.now())
        .clans
        .get(id)

      await(clansProvider.clans).find(_.id == id) match {
        case Some(clan) =>
          if (request.getQueryString("format").contains("json")) {
            Ok(Json.toJson(ClanView(clan, ccw, st)))
          } else
            Ok(
              webTemplateRender.renderTemplate(
                title = Some(s"${clan.fullName}"),
                jsonLink = Some("?format=json")
              )(views.html.clan(clan, ccw, st)))
        case None =>
          NotFound("Clan could not be found")
      }
    }
  }

  def clanwar(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      async {
        implicit val namer: ClanNamer {
          def clanName(id: String): Option[String]
        } = await(namerF)
        implicit val clanner: Clanner {
          def get(id: String): Option[Clan]
        } = await(clannerF)
        implicit val cidtc: ClanIdToClan = await(clanIdToClan)
        await(clanDataProvider.clanwars).all.find(_.id == id) match {
          case Some(clanwar) =>
            if (request.getQueryString("format").contains("json"))
              Ok(Json.toJson(clanwar))
            else
              Ok(
                webTemplateRender.renderTemplate(
                  title = Some(
                    s"${clanwar.clans.flatMap(clanner.get).map(_.fullName).mkString(" and ")} - Clanwar"),
                  jsonLink = Some("?format=json")
                )(views.html.clanwar.contained(views.html.clanwar.clanwar(
                  clanwarMeta = clanwar.meta.named,
                  showPlayers = true,
                  showGames = true,
                  clanwarHtmlPath = WebTemplateRender.wwwLocation.resolve(
                    views.clanwar.Clanwar.ClanwarHtmlFile),
                  renderGame = g =>
                    views.rendergame.Render
                      .renderMixedGame(MixedGame.fromJsonGame(g))
                ))))
          case None => NotFound("Clanwar could not be found")
        }
      }
  }

  def clanwars: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer: ClanNamer {
        def clanName(id: String): Option[String]
      } = await(namerF)
      implicit val clanner: Clanner {
        def get(id: String): Option[Clan]
      } = await(clannerF)
      implicit val cidtc: ClanIdToClan = await(clanIdToClan)
      val cws = await(clanDataProvider.clanwars).all.toList
        .sortBy(_.id)
        .reverse
        .take(50)
      request.getQueryString("format") match {
        case Some("json") =>
          Ok(Json.toJson(cws))
        case _ =>
          Ok(
            webTemplateRender.renderTemplate(
              title = Some("Clanwars"),
              jsonLink = Some("?format=json")
            )(views.html.clanwar
              .contained(views.html.clanwars(cws.map(_.meta.named)))))
      }
    }
  }

  def clans: Action[AnyContent] = Action.async { implicit request =>
    async {
      val clans = await(clansProvider.clans)
      Ok(
        webTemplateRender.renderTemplate(
          title = Some("ActionFPS Clans"),
          jsonLink = Some(configuration.underlying.getString(
            ClansController.ClansReferenceKey))
        )(views.html.clans(clans)))
    }
  }

}

object ClansController {

  val ClansReferenceKey = "af.reference.clans"

  case class ClanView(clan: Clan,
                      recentClanwars: List[Clanwar],
                      stats: Option[Clanstat])

  object ClanView {
    implicit def cww(implicit namer: ClanNamer): Writes[ClanView] = {
      implicit val cstw: OWrites[Clanstat] = Json.writes[Clanstat]
      Json.writes[ClanView]
    }
  }

}
