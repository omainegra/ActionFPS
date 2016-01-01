package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services.{ClansProvider, GamesProvider}

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                clansProvider: ClansProvider,
                                gamesProvider: GamesProvider)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def index = Action.async { implicit request =>
    async {
      val games = await(gamesProvider.getRecent)
      val events = await(gamesProvider.getEvents)
      val latestClanwar = await(clansProvider.latestClanwar)
      await(renderPhp("/")(_.post(
        Map(
          "games" -> Seq(games.toString()),
          "events" -> Seq(events.toString()),
          "clanwars" -> Seq(latestClanwar.toString())
        ))
      ))
    }
  }

  def game(id: String) = Action.async { implicit request =>
    async {
      await(gamesProvider.getGame(id)) match {
        case Some(game) => await(renderPhp("/game.php")(_.post(
          Map("game" -> Seq(game.toString()))
        )))
        case None => NotFound("Game not found")
      }
    }
  }

}