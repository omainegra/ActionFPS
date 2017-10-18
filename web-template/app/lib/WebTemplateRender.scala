package lib

/**
  * Created by William on 01/01/2016.
  */
import java.nio.file.{Files, Path, Paths}
import javax.inject._

import org.jsoup.Jsoup
import play.api.mvc.Results._
import play.api.mvc._
import play.twirl.api.Html

/**
  * Utility to render pages using the main template 'template.html' in 'www' using JSoup.
  *
  * Here we use the DOM Templating technique of manipulating the HTML tree.
  */
class WebTemplateRender @Inject()(controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  /**
    * @param title Set the title tag content to this.
    * @param jsonLink Indicate whether there's JSON support at this page.
    * @param wide whether to render a wide layout. Adds "wide" class to the body.
    * @param html HTML to put inside '#content' element.
    * @param requestHeader implicit request header. Used to extract cookie information.
    * @return rendered HTML.
    */
  def renderTemplate(title: Option[String],
                     jsonLink: Option[String] = None,
                     sourceLink: Boolean = true,
                     wide: Boolean = false)(html: Html)(
      implicit requestHeader: RequestHeader,
      file: sourcecode.File,
      line: sourcecode.Line
  ): Html = {
    val templateHtmlPath =
      WebTemplateRender.wwwLocation.resolve("template.html")
    val js = Jsoup.parse(templateHtmlPath.toFile, "UTF-8")
    title.foreach(js.title)

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
    val prefix = "https://github.com/ScalaWilliam/ActionFPS/blob/master"
    val bottomLinksElement = {
      val el = js.select(WebTemplateRender.navBottomLinks)
      Option(el.first()).foreach(_.children().remove())
      if (el.isEmpty) {
        val nel = js.createElement("nav").attr("id", "bottom-links")
        js.select("#content").first().appendChild(nel)
        nel
      } else el.first()
    }

    if (sourceLink) {
      val lineExtra =
        Some(line.value).filter(_ > 0).map(l => s"#L${l}").getOrElse("")
      val repoFile = file.value.drop(WebTemplateRender.removePrefixLength)
      val link = s"${prefix}${repoFile}${lineExtra}"
      bottomLinksElement
        .appendElement("a")
        .attr("href", link)
        .text("View source (GitHub)")
    }

    jsonLink.foreach { l =>
      bottomLinksElement.appendElement("a").attr("href", l).text("View JSON")
    }

    jsonLink.foreach { l =>
      js.select("head")
        .first()
        .appendElement("link")
        .attr("href", l)
        .attr("type", "application/json")
        .attr("rel", "alternate")
    }

    if (bottomLinksElement.children().isEmpty) {
      bottomLinksElement.remove()
    }

    Html(js.toString)
  }

  def renderStatic(path: Path, wide: Boolean = false)(
      implicit file: sourcecode.File) = Action { implicit r =>
    implicit val line: sourcecode.Line = sourcecode.Line(0)
    Ok(renderTemplate(title = None, wide = wide) {
      Html(
        new String(
          Files.readAllBytes(WebTemplateRender.wwwLocation.resolve(path))))
    })
  }

}

object WebTemplateRender {

  val navBottomLinks = "nav#bottom-links"

  lazy val wwwLocation: Path = {
    List("web/dist/www", "dist/www", "www")
      .map(item => Paths.get(item))
      .find(path => Files.exists(path))
      .getOrElse {
        throw new IllegalArgumentException(s"Could not find 'www'.")
      }
  }

  private val myFile = implicitly[sourcecode.File].value

  val root: String = myFile
    .split("/", -1)
    .reverse
    .dropWhile(_ != "web-template")
    .drop(1)
    .reverse
    .mkString("/")

  val removePrefixLength: Int = root.length

}
