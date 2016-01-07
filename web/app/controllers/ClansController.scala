package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import _root_.clans.Clanstats.ImplicitWrites._
import clans.Clanwar
import clans.Clanwar.ImplicitFormats._
import clans.Conclusion.Namer
import play.api.Configuration
import play.api.libs.json.Json
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
      Ok(renderTemplate(None, false, None)(views.html.clan_rankings(stats)))
    }
  }

  def clan(id: String) = Action.async { implicit request =>
    async {

      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
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
          Ok(renderTemplate(None, false, None)(views.html.clan(clan, ccw, st)))
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
      await(fullProvider.clanwars).all.find(_.id == id) match {
        case Some(clanwar) =>
          Ok(renderTemplate(None, false, None)(views.html.clanwar.clanwar(clanwarMeta = clanwar.meta.named, showPlayers = true, showGames = true)))
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
      import Clanwar.ImplicitFormats._
      val cws = await(fullProvider.clanwars).all.toList.sortBy(_.id).reverse.take(50)
      Ok(renderTemplate(None, false, None)(views.html.clanwars(cws.map(_.meta.named))))
    }
  }

  def clans = Action.async { implicit request =>
    async {
      val clans = await(referenceProvider.clans)
      Ok(renderTemplate(None, false, None)(views.html.clans(clans)))
    }
  }

}