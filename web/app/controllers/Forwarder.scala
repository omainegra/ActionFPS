package controllers

import play.api.libs.ws.WSClient
import play.api.mvc.Action
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

class Forwarder @Inject()(common: Common, wSClient: WSClient)(implicit executionContext: ExecutionContext)
  extends Controller {

  def getAsset(file: String) = Action.async {
    wSClient.url(s"${common.mainPath}/assets/$file")
      .get().map { response =>
      Ok(response.body).as(response.header("Content-Type").get)
    }
  }
}
