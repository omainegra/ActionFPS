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

  def index = Action.async { request =>
    async {
      val games = await(wSClient.url(s"$apiPath/recent/").get()).body
      val events = await(wSClient.url(s"$apiPath/events/").get()).body
      val clanwarsJson = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1").get()).body
      val rendered = await(wSClient.url(s"$mainPath/").post(
        Map(
          "games" -> Seq(games),
          "events" -> Seq(events),
          "clanwars" -> Seq(clanwarsJson)
        ))
      ).body
      Ok(Html(rendered.cleanupPaths))
    }
  }

  def rankings = Action.async { request =>
    async {
      val rankings = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10").get()).body
      val rendered = await(wSClient.url(s"$mainPath/rankings/").post(
        Map("rankings" -> Seq(rankings))
      )).body
      Ok(Html(rendered.cleanupPaths))
    }

  }

  def clan(id: String) = Action.async { request =>
    async {
      val clan = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php").withQueryString("id" -> id).get()).body
      Ok(Html(await(wSClient.url(s"$mainPath/clan/").post(
        Map("clan" -> Seq(clan))
      )).body.cleanupPaths))
    }
  }

  def clanwar(id: String) = Action.async { request =>
    async {
      val clanwar = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwar.php").withQueryString("id" -> id).get()).body
      val render = await(wSClient.url(s"$mainPath/clanwar/").post(
        Map("clanwar" -> Seq(clanwar))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

  def clanwars = Action.async { request =>
    async {
      val clanwars = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50").get()).body
      val render = await(wSClient.url(s"$mainPath/clanwars/").post(
        Map("clanwars" -> Seq(clanwars))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

  def game(id: String) = Action.async { request =>
    async {
      val game = await(wSClient.url(s"${apiPath}/game/").withQueryString("id" -> id).get()).body
      val render = await(wSClient.url(s"$mainPath/game/").post(
        Map("game" -> Seq(game))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

  def clans = Action.async { request =>
    async {
      val clans = await(wSClient.url(s"${apiPath}/clans/").get()).body
      val render = await(wSClient.url(s"$mainPath/clans/").post(
        Map("clans" -> Seq(clans))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

  def players = Action.async { request =>
    async {
      val players = await(wSClient.url(s"${apiPath}/users/").get()).body
      val render = await(wSClient.url(s"$mainPath/players/").post(
        Map("players" -> Seq(players))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

  def player(id: String) = Action.async { request =>
    async {
      require(id.matches("^[a-z]+$"), "Regex must match")
      val player = await(wSClient.url(s"${apiPath}/user/" + id + "/full/").get()).body
      val render = await(wSClient.url(s"$mainPath/player/").withQueryString("id" -> id).post(
        Map("player" -> Seq(player))
      )).body
      Ok(Html(render.cleanupPaths))
    }
  }

}