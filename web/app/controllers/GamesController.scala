package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def index = Action.async { implicit request =>
    async {
      val games = await(wSClient.url(s"$apiPath/recent/").get()).body
      val events = await(wSClient.url(s"$apiPath/events/").get()).body
      val clanwarsJson = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1").get()).body
      await(renderPhp("/")(_.post(
        Map(
          "games" -> Seq(games),
          "events" -> Seq(events),
          "clanwars" -> Seq(clanwarsJson)
        ))
      ))
    }
  }

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

  def game(id: String) = Action.async { implicit request =>
    async {
      val game = await(wSClient.url(s"${apiPath}/game/").withQueryString("id" -> id).get()).body
      await(renderPhp("/game.php")(_.post(
        Map("game" -> Seq(game))
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

  def players = Action.async { implicit request =>
    async {
      val players = await(wSClient.url(s"${apiPath}/users/").get()).body
      await(renderPhp("/players.php")(_.post(
        Map("players" -> Seq(players))
      )))
    }
  }

  def player(id: String) = Action.async { implicit request =>
    async {
      require(id.matches("^[a-z]+$"), "Regex must match")
      val player = await(wSClient.url(s"${apiPath}/user/" + id + "/full/").get()).body
      await(renderPhp("/player.php")(_.withQueryString("id" -> id).post(
        Map("player" -> Seq(player))
      )))
    }
  }

}