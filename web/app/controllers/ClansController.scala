package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import clans.Clanwar
import Clanwar.ImplicitFormats._
import clans.Clanwar
import clans.Conclusion.Namer
import play.api.Configuration
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import providers.{FullProvider, ClansProvider, ReferenceProvider}

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class ClansController @Inject()(common: Common,
                                referenceProvider: ReferenceProvider,
                                clansProvider: ClansProvider,
                                fullProvider: FullProvider)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext) extends Controller {

  import common._

  def rankings = Action.async { implicit request =>
    async {
      val rankings = await(clansProvider.rankings)
      await(renderPhp("/rankings.php")(_.post(
        Map("rankings" -> Seq(rankings.toString()))
      )))
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

      await(clansProvider.clan(id)) match {
        case Some(clan) =>
          await(renderPhp("/clan.php")(_.post(
            Map("clan" -> Seq(clan.toString()),
              "clanwars" -> Seq(Json.toJson(ccw).toString())
            )
          )))
        case None => NotFound("Clan could not be found")
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
          await(renderPhp("/clanwar.php")(_.post(
            Map("clanwar" -> Seq(Json.toJson(clanwar).toString()))
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
      import Clanwar.ImplicitFormats._
      val cws = await(fullProvider.clanwars).all.toList.sortBy(_.id).reverse
      await(renderPhp("/clanwars.php")(_.post(
        Map(
          "clanwars" -> Seq(Json.toJson(cws).toString())
        )
      )))
    }
  }

  def clans = Action.async { implicit request =>
    async {
      val clans = await(referenceProvider.clans)
      await(renderPhp("/clans.php")(_.post(
        Map("clans" -> Seq(Json.toJson(clans).toString))
      )))
    }
  }

}