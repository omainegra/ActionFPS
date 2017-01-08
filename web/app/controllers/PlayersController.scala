package controllers

/**
  * Created by William on 01/01/2016.
  */

import java.nio.file.Files
import javax.inject._

import org.jsoup.Jsoup
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class PlayersController @Inject()(common: Common, referenceProvider: ReferenceProvider,
                                  ladderController: LadderController,
                                  fullProvider: FullProvider)(implicit configuration: Configuration, executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  import common._

  def players: Action[AnyContent] = Action.async { implicit request =>
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

  def hof: Action[AnyContent] = Action.async { implicit request =>
    async {
      val h = await(fullProvider.hof)
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(h))
      else
        Ok(renderTemplate(None, supportsJson = true, None)(views.Hof.render(h)))
    }
  }

  def rankings: Action[AnyContent] = Action.async { implicit request =>
    async {
      val ranks = await(fullProvider.playerRanks).onlyRanked
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(ranks))
      else
        Ok(renderTemplate(None, supportsJson = true, None)(views.PlayerRanks.render(ranks)))
    }
  }

  def player(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          if (request.getQueryString("format").contains("json")) {
            Ok(Json.toJson(player.build))
          } else {
            Ok(renderTemplate(None, supportsJson = true, None)(views.player.Player.render(player, ladderController.agg.get().ranked.find(_.user == id))))
          }
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

  def playerSig(id: String): Action[AnyContent] = Action.async {
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          val build = player.build
          views.player.Signature(interrank =
            build.rank.flatMap(_.rank),
            map = build.favouriteMap,
            playername = build.user.nickname.nickname,
            countrycode = player.build.location.flatMap(_.countryCode),
            ladderrank = ladderController.agg.get().ranked.find(_.user == id).map(_.rank),
            gamecount = player.achievements.map(_.playerStatistics.gamesPlayed)
          ).result
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

}
