package controllers

import javax.inject._

import clans.Clanwar
import clans.Conclusion.Namer
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import providers.games.NewGamesProvider
import providers.{ReferenceProvider, FullProvider}
import services.PingerService

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(common: Common,
                                newGamesProvider: NewGamesProvider,
                                pingerService: PingerService,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext,
                                wSClient: WSClient) extends Controller {

  import common._

  import Clanwar.ImplicitFormats._

  def index = Action.async { implicit request =>
    async {
      implicit val namer = {
        val clans = await(referenceProvider.clans)
        Namer(id => clans.find(_.id == id).map(_.name))
      }
      val games = await(fullProvider.getRecent)
      val events = await(fullProvider.events)
      val latestClanwar = await(fullProvider.clanwars).complete.toList.sortBy(_.id).lastOption
      await(renderJson("/")(
        Map(
          "games" -> Json.toJson(games.map(_.toJson)),
          "events" -> events
        ) ++ latestClanwar.map(lc => "latestClanwar" -> Json.toJson(latestClanwar)).toMap
      ))
    }
  }

  def game(id: String) = Action.async { implicit request =>
    async {
      await(fullProvider.game(id)) match {
        case Some(game) => await(renderJson("/game.php")(
          Map("game" -> game.toJson)
        ))
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