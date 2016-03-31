package controllers

/**
  * Created by William on 01/01/2016.
  */

import java.io.{StringWriter, File}
import java.net.URL
import javax.inject._

import groovy.json.JsonSlurper
import groovy.text.markup.{TemplateResolver, MarkupTemplateEngine, TemplateConfiguration}
import play.api.Configuration
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSResponse, WSRequest, WSClient}
import play.api.mvc.{Result, RequestHeader, AnyContent, Action}
import play.twirl.api.Html

import scala.async.Async._
import play.api.mvc.Results._
import scala.concurrent.{Future, ExecutionContext}

class Common @Inject()(configuration: Configuration)(implicit wsClient: WSClient,
                                                     executionContext: ExecutionContext) {


  def renderTemplate(title: Option[String], supportsJson: Boolean, login: Option[(String, String)])(html: Html)
                    (implicit requestHeader: RequestHeader) = {
    import org.jsoup.Jsoup
    var f = new File("web/dist/www/template.html")
    if ( !f.exists()) {
      f = new File("www/template.html")
    }
    if ( !f.exists() ) {
      f = new File("dist/www/template.html")
    }
    val js = Jsoup.parse(f, "UTF-8")
    title.foreach(js.title)
    if (supportsJson) {
      js.select("#content").attr("data-has-json", "has-json")
    }
    PartialFunction.condOpt(requestHeader.cookies.get("af_id").map(_.value) -> requestHeader.cookies.get("af_name").map(_.value)) {
      case (Some(id), Some(name)) =>
        js.select("#log-in").first().text(name)
        js.select("#log-in").attr("href", s"/player/?id=$id")
        js.select("#download-ac-button").remove()
        js.select("#reg-menu-reg-play").parents().first().remove()
      case _ =>
        js.select("#reg-menu-play").parents().first().remove()
    }
    js.select("#content").html(html.body)
    Html(js.toString)
  }

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
