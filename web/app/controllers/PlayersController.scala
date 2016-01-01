package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider
import providers.players.PlayersProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class PlayersController @Inject()(common: Common, referenceProvider: ReferenceProvider,
                                  playersProvider: PlayersProvider)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def players = Action.async { implicit request =>
    async {
      val players = await(referenceProvider.users)
      await(renderPhp("/players.php")(_.post(
        Map("players" -> Seq(Json.toJson(players).toString()))
      )))
    }
  }

  def player(id: String) = Action.async { implicit request =>
    async {
      require(id.matches("^[a-z]+$"), "Regex must match")
      await(playersProvider.player(id)) match {
        case Some(player) =>
          await(renderPhp("/player.php")(_.withQueryString("id" -> id).post(
            Map("player" -> Seq(player.toString()))
          )))
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

}