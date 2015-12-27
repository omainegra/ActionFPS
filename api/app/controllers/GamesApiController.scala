package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext


/**
  * This API depends on the games
  */
@Singleton
class GamesApiController @Inject()(configuration: Configuration,
                                   gamesService: GamesService,
                                   intersService: IntersService,
                                   newGamesService: NewGamesService)
                                  (implicit executionContext: ExecutionContext) extends Controller {

  def gameIds = Action {
    Ok(Json.toJson(gamesService.allGames.get().map(_.id)))
  }

  def game(id: String) = Action {
    gamesService.allGames.get().find(_.id == id) match {
      case Some(game) => Ok(game.toJson)
      case None => NotFound("Game not found")
    }
  }

  def recentGames = Action {
    Ok(Json.toJson(gamesService.allGames.get().sortBy(_.id).takeRight(50).map(_.toJson)))
  }

  def games(from: Option[String], to: Option[String], limit: Option[Int]) = Action {
    var gms = gamesService
      .allGames
      .get()
      .filter(game => from.isEmpty || from.exists(_ >= game.id))
      .filter(game => to.isEmpty || to.exists(_ <= game.id))
      .sortBy(_.id)
    limit.foreach(l => gms = gms.takeRight(l))
    val enumerator = Enumerator
      .enumerate(
        gms
      )
      .map(game => s"${game.id}\t${game.toJson}\n")
    Ok.chunked(enumerator).as("text/tab-separated-values")
  }

}
