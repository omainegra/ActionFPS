package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
@Singleton
class Masterserver @Inject()(configuration: Configuration)
                            (implicit wSClient: WSClient,
                             executionContext: ExecutionContext)
  extends Controller {

  def apiPath = configuration.underlying.getString("af.apiPath")

  case class SimpleServer(hostname: String, port: Int) {
    def toLine = s"addserver $hostname $port"
  }

  implicit val serverRead = Json.reads[SimpleServer]

  def ms = Action.async {
    async {
      val servahs = await(wSClient.url("http://api.actionfps.com/servers/").get()).json

      val resString = servahs.validate[List[SimpleServer]].get.map(_.toLine).mkString("\n\n")
      Ok(resString).as("text/plain")
    }
  }
}
