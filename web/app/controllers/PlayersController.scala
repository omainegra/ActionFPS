package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import acleague.ranker.achievements.PlayerState
import af.{BuiltProfile, FullProfile}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import players.PlayerStat
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
      request.getQueryString("format") match {
        case Some("registrations-csv") =>
          Ok(await(referenceProvider.Users(withEmails = false).rawRegistrations)).as("text/csv")
        case Some("nicknames-csv") =>
          Ok(await(referenceProvider.Users(withEmails = false).rawNicknames)).as("text/csv")
        case Some("json") =>
          Ok(Json.toJson(await(referenceProvider.Users(withEmails = false).users)))
        case _ =>
          val players = await(referenceProvider.users)
          Ok(renderTemplate(None, supportsJson = true, None)(views.html.players(players)))
      }
    }
  }

  def rankings = Action.async { implicit request =>
    async {
      import _root_.players.PlayersStats.ImplicitWrites._
      val ranks = await(fullProvider.playerRanks).onlyRanked
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(ranks))
      else
        Ok(renderTemplate(None, supportsJson = true, None)(views.html.player_ranks(ranks)))
    }
  }

  def player(id: String) = Action.async { implicit request =>
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          if (request.getQueryString("format").contains("json")) {
            import acleague.ranker.achievements.Jsons._
            implicit val spw = Json.writes[PlayerStat]
            implicit val fpw = Json.writes[BuiltProfile]
            Ok(Json.toJson(player.build))
          } else {
            Ok(renderTemplate(None, supportsJson = true, None)(views.html.player.player(player)))
          }
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

}
