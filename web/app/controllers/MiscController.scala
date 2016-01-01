package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import play.twirl.api.Html
import services.ReferenceProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */

@Singleton
class MiscController @Inject()(common: Common, referenceProvider: ReferenceProvider)
                              (implicit configuration: Configuration,
                               executionContext: ExecutionContext,
                               wSClient: WSClient) extends Controller {

  import common._

  def api = forward("/api/")

  def client = forward("/client/")

  def clientChanges = forward("/client/changes/")

  def questions = forward("/questions/")

  def servers = Action.async { implicit request =>
    async {
      val got = Json.toJson(await(referenceProvider.servers))
      await(renderPhp("/servers.php")(_.post(
        Map("servers" -> Seq(got.toString()))
      )))
    }
  }

  def login = forward("/login/")

  def sync = Action.async(BodyParsers.parse.json) { request =>
    wSClient
      .url(s"$mainPath/sync/")
      .post(request.body)
      .map(response => Ok(Html(response.body)))
  }

  def version = Action {
    val parsedJson = Json.parse(af.BuildInfo.toJson).asInstanceOf[JsObject]
    val two = JsObject(CommitDescription.commitDescription.map(d => "gitCommitDescription" -> JsString(d)).toSeq)
    Ok(parsedJson ++ two)
  }

}