package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import providers.games.{NewGamesProvider, GamesProvider}
import providers.{EventsProvider, ClansProvider}
import services.PingerService

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                clansProvider: ClansProvider,
                                newGamesProvider: NewGamesProvider,
                                pingerService: PingerService,
                                gamesProvider: GamesProvider,
                                eventsProvider: EventsProvider)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def index = Action.async { implicit request =>
    async {
      val games = await(gamesProvider.getRecent)
      val events = await(eventsProvider.getEvents)
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

  def serverUpdates = Action {
    Ok.feed(
      content = pingerService.liveGamesEnum
    ).as("text/event-stream")
  }

  def newGames = Action {
    Ok.feed(
      content = newGamesProvider.newGamesEnum
    ).as("text/event-stream")
  }

}