package controllers

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.Environment
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

/**
  * Created by me on 29/12/2016.
  */
@Singleton
class StaticPageRouter @Inject()(common: Common, environment: Environment) extends SimpleRouter {

  import collection.JavaConverters._

  private def urlPathToLocalPath: List[PageSpec] = Files
    .list(lib.Soup.wwwLocation)
    .iterator()
    .asScala
    .filter(_.toString.endsWith(".html"))
    .map(_.toAbsolutePath)
    .flatMap { path =>
      val soup = Jsoup.parse(path.toFile, "UTF-8")
      PageSpec.unapply(soup, path)
    }.toList

  private def buildRoutes = urlPathToLocalPath.map { spec => {
    case request if request.path == spec.url =>
      common.renderStatic(spec.path, wide = spec.isWide)
  }: Routes
  }.foldLeft(PartialFunction.empty: Routes)(_.orElse(_))

  private lazy val staticRoutes = buildRoutes

  override def routes: Routes = if (environment.isDev) buildRoutes else staticRoutes

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
