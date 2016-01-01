package services

import javax.inject.Inject

import af.rr.ServerRecord
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 01/01/2016.
  */
class ServerProvider @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext, wSClient: WSClient) {

  implicit val serverRecordRead = Json.reads[ServerRecord]
  def servers: Future[List[ServerRecord]] =
    wSClient
      .url("http://api.actionfps.com/servers/")
      .get().map(_.json.validate[List[ServerRecord]].get)
}
