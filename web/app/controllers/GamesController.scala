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

  def game(id: String) = Action.async { implicit request =>
    async {
      val game = await(wSClient.url(s"${apiPath}/game/").withQueryString("id" -> id).get()).body
      await(renderPhp("/game.php")(_.post(
        Map("game" -> Seq(game))
      )))
    }
  }

}