package providers

import javax.inject.Inject

import controllers.Common
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
class EventsProvider @Inject()(common: Common)(implicit executionContext: ExecutionContext,
                                               wSClient: WSClient) {

  import common.apiPath

  def getEvents: Future[JsValue] = {
    wSClient.url(s"$apiPath/events/").get().map(_.json)
  }
}
