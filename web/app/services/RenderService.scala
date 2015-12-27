package services

/**
  * Created by William on 25/12/2015.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RenderService @Inject()(configuration: Configuration,
                             wSClient: WSClient)(implicit executionContext: ExecutionContext) {

  val pathUrl = configuration.underlying.getString("af.render.api.url")

  def query(path: String, data: JsValue): Future[String] = {
    wSClient.url(s"$pathUrl$path").post(data).map(_.body)
  }

}
