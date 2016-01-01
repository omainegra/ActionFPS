package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.http.Writeable
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import play.api.mvc.{Result, RequestHeader, AnyContent, Action}
import play.twirl.api.Html

import scala.async.Async._
import play.api.mvc.Results._
import scala.concurrent.{Future, ExecutionContext}

class Common @Inject()(configuration: Configuration)(implicit wsClient: WSClient,
                                                     executionContext: ExecutionContext) {

  private implicit class cleanHtml(html: String) {
    def cleanupPaths = html
  }

  def renderRaw(path: String)(f: WSRequest => Future[WSResponse]): Future[WSResponse] = {
    f(wsClient.url(s"$mainPath$path"))
  }

  def renderPhp(path: String)(f: WSRequest => Future[WSResponse])
            (implicit request: RequestHeader): Future[Result] = {
    async {
      val extraParams = List("af_id", "af_name").flatMap { key =>
        request.cookies.get(key).map(cookie => key -> cookie.value)
      }
      val rendered = await(f(wsClient.url(s"$mainPath$path")
        .withQueryString(extraParams: _*))).body
      Ok(Html(rendered.cleanupPaths))
    }
  }

  def mainPath = configuration.underlying.getString("af.render.mainPath")

  def apiPath = configuration.underlying.getString("af.apiPath")

  def forward(path: String, id: String): Action[AnyContent] = forward(path, Option(id))

  def forward(path: String, id: Option[String] = None): Action[AnyContent] = Action.async { implicit request =>
    renderPhp(path)(_.withQueryString(id.map(i => "id" -> i).toList: _*).get())
  }


}
