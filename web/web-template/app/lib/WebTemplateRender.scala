package lib

/**
  * Created by William on 01/01/2016.
  */
import java.nio.file.{Files, Path, Paths}
import javax.inject._

import org.jsoup.Jsoup
import play.api.mvc.Results._
import play.api.mvc.{Action, RequestHeader}
import play.twirl.api.Html

/**
  * Utility to render pages using the main template 'template.html' in 'www' using JSoup.
  *
  * Here we use the DOM Templating technique of manipulating the HTML tree.
  */
class WebTemplateRender @Inject()() {

  /**
    * @param title Set the title tag content to this.
    * @param supportsJson Indicate whether there's JSON support at this page.
    *                     Client-side will append a link button to view the JSON at '?format=json'.
    * @param wide whether to render a wide layout. Adds "wide" class to the body.
    * @param html HTML to put inside '#content' element.
    * @param requestHeader implicit request header. Used to extract cookie information.
    * @return rendered HTML.
    */
  def renderTemplate(title: Option[String],
                     supportsJson: Boolean,
                     jsonLink: Option[String] = None,
                     wide: Boolean = false)(html: Html)(
      implicit requestHeader: RequestHeader): Html = {
    val templateHtmlPath =
      WebTemplateRender.wwwLocation.resolve("template.html")
    val js = Jsoup.parse(templateHtmlPath.toFile, "UTF-8")
    title.foreach(js.title)
    if (supportsJson) {
      js.select("#content").attr("data-has-json", "has-json")
      jsonLink.foreach { link =>
        js.select("#content").attr("data-json-link", link)
      }
    }
    if (wide) {
      js.body.addClass("wide")
    }

    js.select(s"#reg-menu-reg-play").first().text("Register & Play!")
    js.select(s"#reg-menu-play").first().text("Play!")

    PartialFunction.condOpt(
      requestHeader.cookies
        .get("af_id")
        .map(_.value) -> requestHeader.cookies.get("af_name").map(_.value)
    ) {
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

  def renderStatic(path: Path, wide: Boolean = false) = Action { implicit r =>
    Ok(renderTemplate(title = None, supportsJson = false, wide = wide) {
      Html(
        new String(
          Files.readAllBytes(WebTemplateRender.wwwLocation.resolve(path))))
    })
  }

}

object WebTemplateRender {
  lazy val wwwLocation: Path = {
    List("web/dist/www", "dist/www", "www")
      .map(item => Paths.get(item))
      .find(path => Files.exists(path))
      .getOrElse {
        throw new IllegalArgumentException(s"Could not find 'www'.")
      }
  }
}
