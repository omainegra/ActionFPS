package controllers

import javax.inject._

import clans.Clanwar
import clans.Conclusion.Namer
import play.api.Configuration
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import providers.full.FullProvider
import providers.games.NewGamesProvider
import providers.ReferenceProvider
import services.PingerService
import views.rendergame.MixedGame

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
      val games = await(fullProvider.getRecent).map(MixedGame.fromJsonGame)
      val events = await(fullProvider.events)
      val latestClanwar = await(fullProvider.clanwars).complete.toList.sortBy(_.id).lastOption
      Ok(renderTemplate(None, true, None)(views.html.index(games = games, events = events)))
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