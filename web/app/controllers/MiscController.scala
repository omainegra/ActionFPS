package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BodyParsers, Controller}
import play.twirl.api.Html
import providers.ReferenceProvider

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

  def servers: Action[AnyContent] = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("json") =>
          Ok(Json.toJson(await(referenceProvider.Servers.servers)))
        case _ =>
          val got = await(referenceProvider.servers)
          Ok(renderTemplate(None, supportsJson = true, None)(views.html.servers(got)))
      }
    }
  }

  def sync: Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
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
