package controllers

import javax.inject._

import com.actionfps.clans.Conclusion.Namer
import lib.WebTemplateRender
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider
import providers.games.NewGamesProvider
import views.rendergame.MixedGame

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class GamesController @Inject()(webTemplateRender: WebTemplateRender,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider)
                               (implicit executionContext: ExecutionContext) extends Controller {

  private implicit def namerF = async {
    val clans = await(referenceProvider.clans)
    Namer(id => clans.find(_.id == id).map(_.name))
  }

  def recentGames: Action[AnyContent] = Action.async { implicit request =>
    async {
      val games = await(fullProvider.getRecent(100)).map(MixedGame.fromJsonGame)
      Ok(webTemplateRender.renderTemplate(
        title = Some("Recent ActionFPS Games"),
        supportsJson = false
      )(
        views.html.recent_games(games)
      ))
    }
  }

  def game(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      await(fullProvider.game(id)) match {
        case Some(game) =>
          if (request.getQueryString("format").contains("json"))
            Ok(Json.toJson(game))
          else
            Ok(webTemplateRender.renderTemplate(
              title = Some {
                val namer = await(namerF)
                game.clangame.toList.flatMap(_.toList).flatMap(namer.clan) match {
                  case a :: b :: Nil => s"Clan game between ${a} and ${b}"
                  case _ =>
                    s"Game between ${game.teams.flatMap(_.players).flatMap(_.user).mkString(", ")}"
                }
              },
              supportsJson = true
            )(views.html.game(game)))
        case None => NotFound("Game not found")
      }
    }
  }

}
