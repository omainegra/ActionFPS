package services

/**
  * Created by William on 28/12/2015.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.api.mvc.{Results, Result, RequestHeader}
import play.twirl.api.Html

import scala.concurrent.{Future, ExecutionContext}

class PhpRenderService @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext, wSClient: WSClient) {

  // todo pass through cookie info to PHP
  def root = configuration.underlying.getString("af.render.api.url")

  def apply(path: String, json: JsValue)(implicit requestHeader: RequestHeader): Future[Result] = {
    import Results._
    if (requestHeader.getQueryString("format").contains("json")) {
      Future.successful(Ok(json))
    } else {
      render(path, json).map(html => Ok(html))
    }
  }

  def render(path: String, json: JsValue)(implicit requestHeader: RequestHeader): Future[Html] = {
    val url = s"$root$path"
    val reqp = List("af_name", "af_id").flatMap(num => requestHeader.cookies.get(num).map(v => num -> v.value))
    wSClient.url(url).withQueryString(reqp:_*).post(json).map(response =>
      Html(response.body)
    )
  }

  def renderStatelessRaw(path: String, json: JsValue): Future[String] = {
    wSClient.url(s"$root$path").post(json).map(_.body)
  }
}
