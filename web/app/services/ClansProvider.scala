package services

import javax.inject.Inject

import af.Clan
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 01/01/2016.
  */
class ClansProvider @Inject()()(implicit wSClient: WSClient, executionContext: ExecutionContext) {
  import controllers.cf
  def clans: Future[List[Clan]] = {
    wSClient.url("http://api.actionfps.com/clans/").get().map(
      _.json.validate[List[Clan]].get
    )
  }
}
