package controllers

import javax.inject._

import com.actionfps.clans.ClanNamer
import lib.WebTemplateRender
import play.api.libs.json.Json
import play.api.mvc._
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext
import com.actionfps.formats.json.Formats._

@Singleton
class GamesController @Inject()(webTemplateRender: WebTemplateRender,
                                providesClanNames: ProvidesClanNames,
                                providesGames: ProvidesGames,
                                components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components) {

  def recentGames: Action[AnyContent] = Action.async { implicit request =>
    async {
      val games =
        await(providesGames.getRecent(GamesController.NumberOfRecentGames))
          .map(MixedGame.fromJsonGame)
      Ok(
        webTemplateRender.renderTemplate(
          title = Some("Recent ActionFPS Games")
        )(
          views.html.recent_games(games)
        ))
    }
  }

  def game(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      await(providesGames.game(id)) match {
        case Some(game) =>
          if (request.getQueryString("format").contains("json"))
            Ok(Json.toJson(game))
          else
            Ok(
              webTemplateRender.renderTemplate(
                title = Some {
                  val clanNames = await(providesClanNames.clanNames)
                  game.clangame.toList
                    .flatMap(_.toList)
                    .flatMap(clanNames.get) match {
                    case a :: b :: Nil => s"Clan game between ${a} and ${b}"
                    case _ =>
                      s"Game between ${game.teams.flatMap(_.players).flatMap(_.user).mkString(", ")}"
                  }
                },
                jsonLink = Some("?format=json")
              )(views.html.game(game)))
        case None => NotFound("Game not found")
      }
    }
  }

}

object GamesController {
  val NumberOfRecentGames = 100
}
