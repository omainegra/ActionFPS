package providers

import javax.inject._

import controllers.Common
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 01/01/2016.
  */

@Singleton
class ClansProvider @Inject()(common: Common)(implicit executionContext: ExecutionContext, wSClient: WSClient) {

  def rankings: Future[JsValue] = {
    wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10").get().map(_.json)
  }

  def clan(id: String): Future[Option[JsValue]] = {
    wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php").withQueryString("id" -> id).get()
      .map(_.json).map(Option.apply)
  }

}
