package services

import javax.inject._

import controllers.Common
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */

@Singleton
class PlayersProvider @Inject()(common: Common)(implicit executionContext: ExecutionContext, wSClient: WSClient) {


  def player(id: String): Future[Option[JsValue]] = {
    val regex = "^[a-z]+$".r
    import scala.async.Async._
    async {
      id match {
        case regex() =>
          Option(await(wSClient.url(s"${common.apiPath}/user/" + id + "/full/").get()).json)
        case _ =>
          None
      }
    }
  }

}
