package providers

import javax.inject.Inject

import af.rr.ServerRecord
import af.{User, Clan}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 01/01/2016.
  */
class ReferenceProvider @Inject()()(implicit wSClient: WSClient, executionContext: ExecutionContext) {

  import controllers.cf

  def clans: Future[List[Clan]] = {
    wSClient.url("http://api.actionfps.com/clans/").get().map(
      _.json.validate[List[Clan]].get
    )
  }

  def users: Future[List[User]] = wSClient.url("http://api.actionfps.com/users/").get().map { response =>
    response.json.validate[List[User]].get
  }

  implicit val serverRecordRead = Json.reads[ServerRecord]

  def servers: Future[List[ServerRecord]] =
    wSClient
      .url("http://api.actionfps.com/servers/")
      .get().map(_.json.validate[List[ServerRecord]].get)
}
