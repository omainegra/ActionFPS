package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{ClansProvider, ReferenceProvider}

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class ClansController @Inject()(common: Common,
                                referenceProvider: ReferenceProvider,
                                clansProvider: ClansProvider)
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
      await(clansProvider.clan(id)) match {
        case Some(clan) =>
          await(renderPhp("/clan.php")(_.post(
            Map("clan" -> Seq(clan.toString()))
          )))
        case None => NotFound("Clan could not be found")
      }
    }
  }

  def clanwar(id: String) = Action.async { implicit request =>
    async {
      await(clansProvider.clanwar(id)) match {
        case Some(clanwar) =>
          await(renderPhp("/clanwar.php")(_.post(
            Map("clanwar" -> Seq(clanwar.toString()))
          )))
        case None => NotFound("Clanwar could not be found")
      }
    }
  }

  def clanwars = Action.async { implicit request =>
    async {
      val clanwars = await(clansProvider.clanwars)
      await(renderPhp("/clanwars.php")(_.post(
        Map("clanwars" -> Seq(clanwars.toString()))
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