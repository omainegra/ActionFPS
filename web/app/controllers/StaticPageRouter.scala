package controllers

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}

import lib.WebTemplateRender
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.Environment
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

/**
  * Created by me on 29/12/2016.
  *
  * Serve static pages and render them in the template.
  */
@Singleton
class StaticPageRouter @Inject()(common: WebTemplateRender,
                                 environment: Environment,
                                 components: ControllerComponents)
    extends AbstractController(components)
    with SimpleRouter {

  import collection.JavaConverters._

  private def urlPathToLocalPath: List[PageSpec] =
    Files
      .list(WebTemplateRender.wwwLocation)
      .iterator()
      .asScala
      .filter(_.toString.endsWith(".html"))
      .map(_.toAbsolutePath)
      .flatMap { path =>
        val soup = Jsoup.parse(path.toFile, "UTF-8")
        PageSpec.unapply(soup, path)
      }
      .toList

  private def buildRoutes =
    urlPathToLocalPath
      .map { spec =>
        {
          case request if request.path == spec.url =>
            val pathItem = spec.path.toString.drop(
              WebTemplateRender.wwwLocation.toAbsolutePath.toString.length)
            implicit val sourceFile: sourcecode.File = sourcecode.File(
              WebTemplateRender.root + "/web/dist/www/" + pathItem)
            common.renderStatic(spec.path, wide = spec.isWide)
        }: Routes
      }
      .foldLeft(PartialFunction.empty: Routes)(_.orElse(_))

  private lazy val staticRoutes = buildRoutes

  override def routes: Routes =
    if (environment.isDev) buildRoutes else staticRoutes

}

case class PageSpec(document: Document, url: String, path: Path) {
  def isWide: Boolean = {
    document.select("article").hasClass("html-wide")
  }
}

object PageSpec {
  def unapply(soup: Document, path: Path): Option[PageSpec] = {
    val url = soup.select("link[rel='canonical']").first()
    if (url != null) Some(PageSpec(soup, url.attr("href"), path)) else None
  }
}
