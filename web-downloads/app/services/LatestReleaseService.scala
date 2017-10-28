package services

import org.apache.http.client.HttpClient
import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.cache.CachingHttpClientBuilder
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import play.api.libs.json.Json
import services.LatestReleaseService.ReleaseLinks

import scala.concurrent._

/**
  * Created by me on 04/02/2017.
  *
  * Load the latest release from GitHub Releases.
  * Uses Cached HTTP Client to avoid request limits.
  */
class LatestReleaseService(implicit executionContext: ExecutionContext) {
  private val client: CloseableHttpClient =
    CachingHttpClientBuilder.create().build()
  private val context = HttpCacheContext.create()

  def latestRelease(): ReleaseLinks = {
    ReleaseLinks.getLatestRelease(client, context)
  }

  def latestItemFuture(): Future[ReleaseLinks] = {
    Future(concurrent.blocking(latestRelease()))
  }

}

object LatestReleaseService {

  case class ReleaseLinks(windowsLink: Option[String],
                          linuxLink: Option[String],
                          osxLink: Option[String])

  object ReleaseLinks {

    def getLatestRelease(httpClient: HttpClient,
                         httpContext: HttpContext): ReleaseLinks = {
      val theUrl =
        "https://api.github.com/repos/ActionFPS/ActionFPS-Game/releases/latest"
      val httpGet = new HttpGet(theUrl)
      val resp = httpClient.execute(httpGet, httpContext)
      val str = EntityUtils.toString(resp.getEntity)
      val json = Json.parse(str)
      val links = (json \\ "browser_download_url").flatMap(_.asOpt[String])
      ReleaseLinks(
        windowsLink = links
          .find(_.endsWith(".exe"))
          .orElse(links.find(_.endsWith(".msi"))),
        linuxLink = links
          .find(_.endsWith(".bz2"))
          .orElse(links.find(_.endsWith(".deb"))),
        osxLink = links.find(_.endsWith(".dmg"))
      )
    }
  }

}
