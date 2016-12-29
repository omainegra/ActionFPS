package controllers

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}

import org.jsoup.Jsoup
import play.Environment
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

/**
  * Created by me on 29/12/2016.
  */
@Singleton
class StaticPageRouter @Inject()(common: Common, environment: Environment) extends SimpleRouter {

  import collection.JavaConverters._

  private def urlPathToLocalPath: List[(String, Path)] = Files
    .list(lib.Soup.wwwLocation)
    .iterator()
    .asScala
    .filter(_.toString.endsWith(".html"))
    .map(_.toAbsolutePath)
    .flatMap { path =>
      val soup = Jsoup.parse(path.toFile, "UTF-8")
      val url = soup.select("link[rel='canonical']").first()
      if (url != null) Some(url.attr("href") -> path) else None
    }.toList

  private def buildRoutes = urlPathToLocalPath.map { case (url, path) => {
    case request if request.path == url => common.renderStatic(path)
  }: Routes
  }.foldLeft(PartialFunction.empty: Routes)(_.orElse(_))

  private lazy val staticRoutes = buildRoutes

  override def routes: Routes = if (environment.isDev) buildRoutes else staticRoutes

}
