package providers.games

import javax.inject.{Inject, Singleton}

import akka.agent.Agent
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import com.actionfps.formats.json.Formats._
import play.api.libs.json.Json

/**
  * Created by William on 01/01/2016.
  *
  */
@Singleton
class BatchURLGamesProvider @Inject()(configuration: Configuration)
                                     (implicit executionContext: ExecutionContext,
                                    wSClient: WSClient) extends GamesProvider {

  private def allPath = configuration.underlying.getString("af.reference.games")

  private def fetchAllGames = wSClient.url(allPath).get().map(response =>
    response.body.split("\n").toIterator.map { line =>
      line.split("\t").toList match {
        case List(id, json) =>
          id -> Json.fromJson[Game](Json.parse(json)).get.flattenPlayers
      }
    }.toMap
  )

  private val allGamesFA = fetchAllGames.map(m => Agent(m))

  override def games: Future[Map[String, JsonGame]] = allGamesFA.map(_.get())
}
