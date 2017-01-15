package services

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.cache.CachingHttpClientBuilder
import org.apache.http.util.EntityUtils
import play.twirl.api.Html
import services.NewsService.NewsItem

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 12/01/2017.
  *
  * Load the details of the latest ActionFPS article to be displayed in [[controllers.IndexController.index]].
  *
  * @see We use a Apache's Cached HTTP Client.
  *      [[https://hc.apache.org/httpcomponents-client-ga/tutorial/html/caching.html]]
  *
  */
@Singleton
class NewsService @Inject()(implicit executionContext: ExecutionContext) {
  private val client: CloseableHttpClient = CachingHttpClientBuilder.create().build()
  private val context = HttpCacheContext.create()
  private val atomUrl = "https://actionfps.blogspot.com/feeds/posts/default"

  def latestItem(): Future[NewsItem] = {
    Future {
      concurrent.blocking {
        val atomContent = EntityUtils.toString(client.execute(new HttpGet(atomUrl), context).getEntity)
        NewsService.extractNewsItem(scala.xml.XML.loadString(atomContent))
      }
    }
  }

}

object NewsService {

  def extractNewsItem(xml: scala.xml.Elem): NewsItem = {
    val entry = (xml \\ "entry").head

    val link = (entry \ "link")
      .filter(l => (l \ "@rel").text == "alternate")
      .filter(l => (l \ "@type").text == "text/html")
      .head
    val published = ZonedDateTime.parse((entry \ "published").head.text)
    val title = (link \ "@title").text
    val url = (link \ "@href").text
    NewsItem(published, title, url)
  }

  case class NewsItem(postDate: ZonedDateTime, title: String, url: String) {
    private def formattedTime = postDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    def htmlContent: Html = {
      Html(<time is="relative-time" datetime={formattedTime}>
        {formattedTime}
      </time>.toString +
        <a href={url} target="_blank">
          {title}
        </a>.toString)
    }
  }

}
