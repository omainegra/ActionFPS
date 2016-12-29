package controllers

import javax.inject._

import com.actionfps.reference.HeadingsRecord
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

  def headings: Action[AnyContent] = Action.async { request =>
    async {
      request.getQueryString("format") match {
        case Some("csv") =>
          Ok(await(referenceProvider.Headings.csv))
        case Some("latest") =>
          await(referenceProvider.bulletin).map(_.html.body) match {
            case Some(html) => Ok(html)
            case None => NotFound("Latest bulletin could not be found")
          }
        case _ =>
          implicit val wrHr = Json.writes[HeadingsRecord]
          Ok(Json.toJson(await(referenceProvider.Headings.headings)))
      }
    }
  }

  def servers: Action[AnyContent] = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("csv") =>
          Ok(await(referenceProvider.Servers.raw)).as("text/csv")
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
