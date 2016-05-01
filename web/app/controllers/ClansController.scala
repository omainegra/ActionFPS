package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import com.actionfps.accumulation.Clan
import com.actionfps.clans.Clanstats.ImplicitWrites._
import com.actionfps.clans.{Clanstat, Clanwar}
import com.actionfps.clans.Clanwar.ImplicitFormats._
import com.actionfps.clans.Conclusion.Namer
import lib.Clanner
import play.api.Configuration
import play.api.libs.json.{Writes, Json}
import play.api.mvc.{Action, Controller}
import providers.full.FullProvider
import providers.ReferenceProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class ClansController @Inject()(common: Common,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext) extends Controller {

  import common._

  def rankings = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }

      val stats = await(fullProvider.clanstats).onlyRanked.named
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(stats))
      else
        Ok(renderTemplate(None, supportsJson = true, None)(views.html.clan_rankings(stats)))
    }
  }

  case class ClanView(clan: Clan, recentClanwars: List[Clanwar], stats: Option[Clanstat])

  def clan(id: String) = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }

      implicit val cww = {
        implicit val cstw = Json.writes[Clanstat]
        Json.writes[ClanView]
      }

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
            Ok(renderTemplate(None, supportsJson = true, None)(views.html.clan(clan, ccw, st)))
        case None =>
          NotFound("Clan could not be found")
      }
    }
  }

  def clanwar(id: String) = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      implicit val clanner = {
        val clans = await(referenceProvider.clans)
        Clanner(id => clans.find(_.id == id))
      }
      await(fullProvider.clanwars).all.find(_.id == id) match {
        case Some(clanwar) =>
          if (request.getQueryString("format").contains("json"))
            Ok(Json.toJson(clanwar))
          else
            Ok(renderTemplate(
              title = None,
              supportsJson = true,
              login = None)(views.html.clanwar.clanwar(
              clanwarMeta = clanwar.meta.named,
              showPlayers = true,
              showGames = true
            )))
        case None => NotFound("Clanwar could not be found")
      }
    }
  }

  def clanwars = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      implicit val clanner = {
        val clans = await(referenceProvider.clans)
        Clanner(id => clans.find(_.id == id))
      }
      import Clanwar.ImplicitFormats._
      val cws = await(fullProvider.clanwars).all.toList.sortBy(_.id).reverse.take(50)
      request.getQueryString("format") match {
        case Some("json") =>
          Ok(Json.toJson(cws))
        case _ => Ok(renderTemplate(None, supportsJson = true, None)(views.html.clanwars(cws.map(_.meta.named))))
      }
    }
  }

  def clans = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("csv") =>
          Ok(await(referenceProvider.Clans.csv)).as("text/csv")
        case Some("json") =>
          Ok(Json.toJson(await(referenceProvider.Clans.clans)))
        case _ =>
          val clans = await(referenceProvider.clans)
          Ok(renderTemplate(None, supportsJson = true, None)(views.html.clans(clans)))
      }
    }
  }

}