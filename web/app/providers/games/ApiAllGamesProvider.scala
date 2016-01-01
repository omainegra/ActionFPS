package providers.games

import javax.inject.{Inject, Singleton}

import acleague.enrichers.JsonGame
import akka.agent.Agent
import controllers.Common
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
class ApiAllGamesProvider @Inject()(common: Common)
                                   (implicit executionContext: ExecutionContext,
                                    wSClient: WSClient) extends GamesProvider {

  import common.apiPath

  def fetchAllGames = wSClient.url(s"${apiPath}/all/").get().map(response =>
    response.body.split("\n").toIterator.map { line =>
      line.split("\t").toList match {
        case List(id, json) =>
          id -> JsonGame.fromJson(json)
      }
    }.toMap
  )

  val allGamesFA = fetchAllGames.map(m => Agent(m))

  override def games: Future[Map[String, JsonGame]] = allGamesFA.map(_.get())
}
