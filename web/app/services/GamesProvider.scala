package services

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import acleague.enrichers.JsonGame
import controllers.Common
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class GamesProvider @Inject()(common: Common)(implicit executionContext: ExecutionContext,
                                              wSClient: WSClient) {

  import common.apiPath

  def getGame(id: String): Future[Option[JsValue]] = {
    async {
      Option(await(wSClient.url(s"${apiPath}/game/").withQueryString("id" -> id).get()).json)
    }
  }

  def getEvents: Future[JsValue] = {
    wSClient.url(s"$apiPath/events/").get().map(_.json)
  }

  def getRecent: Future[JsValue] = {
    wSClient.url(s"$apiPath/recent/").get().map(_.json)
  }

}
