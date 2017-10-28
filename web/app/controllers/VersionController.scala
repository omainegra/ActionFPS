package controllers

import java.util.Base64

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */
class VersionController(controllerComponents: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def version = Action {
    val parsedJson = Json.parse(af.BuildInfo.toJson).asInstanceOf[JsObject]
    val two = JsObject(
      VersionController.commitDescription
        .map(d => "gitCommitDescription" -> JsString(d))
        .toSeq)
    Ok(parsedJson ++ two)
  }

}

object VersionController {
  val commitDescription: Option[String] = {
    af.BuildInfo.gitCommitDescription.map { encoded =>
      new String(Base64.getDecoder.decode(encoded), "UTF-8")
    }
  }
}
