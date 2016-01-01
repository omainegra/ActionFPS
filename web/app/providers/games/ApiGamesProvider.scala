package providers.games

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import acleague.enrichers.JsonGame
import controllers.Common
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Get games from legacy endpoints. Useful for development as quite fast.
  */
@Singleton
class ApiGamesProvider @Inject()(common: Common)(implicit executionContext: ExecutionContext,
                                                 wSClient: WSClient) extends GamesProvider {

  import common.apiPath

  def getGame(id: String): Future[Option[JsValue]] = {
    async {
      Option(await(wSClient.url(s"${apiPath}/game/").withQueryString("id" -> id).get()).json)
    }
  }

  def getRecent: Future[JsValue] = {
    wSClient.url(s"$apiPath/recent/").get().map(_.json)
  }

}
