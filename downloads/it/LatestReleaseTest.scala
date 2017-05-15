import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.impl.client.cache.CachingHttpClientBuilder
import org.scalatest.{FreeSpec, Matchers}
import services.LatestReleaseService.ReleaseLinks

/**
  * Created by me on 04/02/2017.
  */
class LatestReleaseTest extends FreeSpec with Matchers {
  "It works" in {
    val client = CachingHttpClientBuilder.create().build()
    val context = HttpCacheContext.create()
    val latestRelease =
      try ReleaseLinks.getLatestRelease(client, context)
      finally client.close()
    info(s"Latest release = ${latestRelease}")
  }
}
