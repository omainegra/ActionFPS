package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import akka.stream.scaladsl.Source
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import providers.full.FullProvider

import scala.concurrent.ExecutionContext

class ApiController @Inject()(fullProvider: FullProvider)
                             (implicit executionContext: ExecutionContext) extends Controller {
  def all = Action.async {
    fullProvider.allGames.map { games =>
      val gamesSource = Source(games)
        .map(game => s"${game.id}\t${Json.toJson(game)}\n")
      Ok.chunked(gamesSource).as("text/tab-separated-values")
    }
  }
}
