package controllers

import javax.inject._

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class VersionController @Inject()()(
    implicit executionContext: ExecutionContext)
    extends Controller {

  def version = Action {
    val parsedJson = Json.parse(af.BuildInfo.toJson).asInstanceOf[JsObject]
    val two = JsObject(
      CommitDescription.commitDescription
        .map(d => "gitCommitDescription" -> JsString(d))
        .toSeq)
    Ok(parsedJson ++ two)
  }

}
