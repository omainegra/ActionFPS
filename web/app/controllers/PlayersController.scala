package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import providers.full.FullProvider
import providers.ReferenceProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class PlayersController @Inject()(common: Common, referenceProvider: ReferenceProvider,
                                  fullProvider: FullProvider)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def players = Action.async { implicit request =>
    async {
      val players = await(referenceProvider.users)
      await(renderJson("/players.php")(
        Map("players" -> Json.toJson(players)
      )))
    }
  }

  def rankings = Action.async { implicit request =>
    async {
      import _root_.players.PlayersStats.ImplicitWrites._
      val ranks = await(fullProvider.playerRanks).onlyRanked
      await(renderJson("/playerranks.php")(Map(
        "ranks" -> Json.toJson(ranks)
      )))
    }
  }

  def player(id: String) = Action.async { implicit request =>
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          await(renderJsonWR("/player.php")(_.withQueryString("id" -> id))(
            Map("player" -> player.toJson)
          ))
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

}