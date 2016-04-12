package providers.games

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.JsonGame
import akka.agent.Agent
import controllers.Common
import play.api.Configuration
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  *
  * Get games from the /all/ endpoint of the public API.
  * This endpoint is guaranteed to be there forever.
  * TODO caching / indexing
  */
@Singleton
class ApiAllGamesProvider @Inject()(configuration: Configuration)
                                   (implicit executionContext: ExecutionContext,
                                    wSClient: WSClient) extends GamesProvider {

  def allPath = configuration.underlying.getString("af.reference.games")

  def fetchAllGames = wSClient.url(allPath).get().map(response =>
    response.body.split("\n").toIterator.map { line =>
      line.split("\t").toList match {
        case List(id, json) =>
          id -> JsonGame.fromJson(json).flattenPlayers
      }
    }.toMap
  )

  val allGamesFA = fetchAllGames.map(m => Agent(m))

  override def games: Future[Map[String, JsonGame]] = allGamesFA.map(_.get())
}
