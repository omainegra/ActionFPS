package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class ClansController @Inject()(common: Common)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def rankings = Action.async { implicit request =>
    async {
      val rankings = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10").get()).body
      await(renderPhp("/rankings.php")(_.post(
        Map("rankings" -> Seq(rankings))
      )))
    }
  }

  def clan(id: String) = Action.async { implicit request =>
    async {
      val clan = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php").withQueryString("id" -> id).get()).body
      await(renderPhp("/clan.php")(_.post(
        Map("clan" -> Seq(clan))
      )))
    }
  }

  def clanwar(id: String) = Action.async { implicit request =>
    async {
      val clanwar = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwar.php").withQueryString("id" -> id).get()).body
      await(renderPhp("/clanwar.php")(_.post(
        Map("clanwar" -> Seq(clanwar))
      )))
    }
  }

  def clanwars = Action.async { implicit request =>
    async {
      val clanwars = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50").get()).body
      await(renderPhp("/clanwars.php")(_.post(
        Map("clanwars" -> Seq(clanwars))
      )))
    }
  }

  def clans = Action.async { implicit request =>
    async {
      val clans = await(wSClient.url(s"${apiPath}/clans/").get()).body
      await(renderPhp("/clans.php")(_.post(
        Map("clans" -> Seq(clans))
      )))
    }
  }

}