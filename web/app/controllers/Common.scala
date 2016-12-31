package controllers

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._

import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.mvc.Results._
import play.api.mvc.{Action, RequestHeader}
import play.twirl.api.Html

import scala.concurrent.ExecutionContext

class Common @Inject()(configuration: Configuration
                      )(implicit wsClient: WSClient,
                        val messagesApi: MessagesApi, executionContext: ExecutionContext) extends I18nSupport {


  def renderTemplate(title: Option[String], supportsJson: Boolean, login: Option[(String, String)], wide: Boolean = false)(html: Html)
                    (implicit requestHeader: RequestHeader): Html = {
    import org.jsoup.Jsoup
    var f = new File("web/dist/www/template.html")
    if (!f.exists()) {
      f = new File("www/template.html")
    }
    if (!f.exists()) {
      f = new File("dist/www/template.html")
    }
    val js = Jsoup.parse(f, "UTF-8")
    title.foreach(js.title)
    if (supportsJson) {
      js.select("#content").attr("data-has-json", "has-json")
    }
    if (wide) {
      js.body.addClass("wide")
    }

    js.select(s"#reg-menu-reg-play").first().text(Messages("menu.register-play"))
    js.select(s"#reg-menu-play").first().text(Messages("menu.play"))

    Set("clan-ranks", "clan-wars", "official-clans", "player-ranks", "hall-of-fame", "servers", "faq", "forum", "login").foreach {
      id => js.select(s"#menu-${id} a").first().text(Messages(s"menu.${id}"))
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

  def mainPath: String = configuration.underlying.getString("af.render.mainPath")

  def apiPath: String = configuration.underlying.getString("af.apiPath")

  def renderStatic(path: Path, wide: Boolean = false) = Action { implicit r =>
    Ok(renderTemplate(title = None, supportsJson = false, wide = wide,
      login = None) {
      Html(new String(Files.readAllBytes(lib.Soup.wwwLocation.resolve(path))))
    })
  }

}
