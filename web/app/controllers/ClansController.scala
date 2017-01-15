package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import com.actionfps.accumulation.Clan
import com.actionfps.clans.Clanwar
import com.actionfps.clans.Conclusion.Namer
import com.actionfps.stats.Clanstat
import controllers.ClansController.ClanView
import lib.{Clanner, WebTemplateRender}
import play.api.Configuration
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider
import views.ClanRankings

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class ClansController @Inject()(webTemplateRender: WebTemplateRender,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext) extends Controller {

  private def namerF = async {
    val clans = await(referenceProvider.clans)
    Namer(id => clans.find(_.id == id).map(_.name))
  }

  private def clannerF = async {
    val clans = await(referenceProvider.clans)
    Clanner(id => clans.find(_.id == id))
  }

  def rankings: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = await(namerF)
      val stats = await(fullProvider.clanstats).onlyRanked.named
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(stats))
      else
        Ok(webTemplateRender.renderTemplate(
          title = Some("Clan Rankings"),
          supportsJson = true
        )(ClanRankings.render(stats)))
    }
  }

  def clan(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = await(namerF)

      val ccw = await(fullProvider.clanwars)
        .all
        .filter(_.clans.contains(id))
        .toList
        .sortBy(_.id)
        .reverse
        .take(15)

      val st = await(fullProvider.clanstats).clans.get(id)

      await(referenceProvider.clans).find(_.id == id) match {
        case Some(clan) =>
          if (request.getQueryString("format").contains("json")) {
            Ok(Json.toJson(ClanView(clan, ccw, st)))
          } else
            Ok(webTemplateRender.renderTemplate(
              title = Some(s"${clan.fullName}"),
              supportsJson = true
            )(views.html.clan(clan, ccw, st)))
        case None =>
          NotFound("Clan could not be found")
      }
    }
  }

  def clanwar(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = await(namerF)
      implicit val clanner = await(clannerF)
      await(fullProvider.clanwars).all.find(_.id == id) match {
        case Some(clanwar) =>
          if (request.getQueryString("format").contains("json"))
            Ok(Json.toJson(clanwar))
          else
            Ok(webTemplateRender.renderTemplate(
              title = Some(s"Clanwar between ${clanwar.clans.flatMap(clanner.get).mkString(" and ")}"),
              supportsJson = true
            )(views.html.clanwar.clanwar(
              clanwarMeta = clanwar.meta.named,
              showPlayers = true,
              showGames = true
            )))
        case None => NotFound("Clanwar could not be found")
      }
    }
  }

  def clanwars: Action[AnyContent] = Action.async { implicit request =>
    async {
      implicit val namer = await(namerF)
      implicit val clanner = await(clannerF)
      val cws = await(fullProvider.clanwars).all.toList.sortBy(_.id).reverse.take(50)
      request.getQueryString("format") match {
        case Some("json") =>
          Ok(Json.toJson(cws))
        case _ => Ok(webTemplateRender.renderTemplate(
          title = Some("Clanwars"),
          supportsJson = true
        )(views.html.clanwars(cws.map(_.meta.named))))
      }
    }
  }

  def clans: Action[AnyContent] = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("csv") =>
          Ok(await(referenceProvider.Clans.csv)).as("text/csv")
        case Some("json") =>
          Ok(Json.toJson(await(referenceProvider.Clans.clans)))
        case _ =>
          val clans = await(referenceProvider.clans)
          Ok(webTemplateRender.renderTemplate(
            title = Some("ActionFPS Clans"),
            supportsJson = true
          )(views.html.clans(clans)))
      }
    }
  }

}

object ClansController {

  case class ClanView(clan: Clan, recentClanwars: List[Clanwar], stats: Option[Clanstat])

  object ClanView {
    implicit def cww(implicit namer: Namer): Writes[ClanView] = {
      implicit val cstw = Json.writes[Clanstat]
      Json.writes[ClanView]
    }
  }

}
