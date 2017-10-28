package services

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import org.apache.http.client.cache.HttpCacheContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.cache.CachingHttpClientBuilder
import org.apache.http.util.EntityUtils
import play.twirl.api.Html
import services.NewsService.NewsItems

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, Node}

/**
  * Created by me on 12/01/2017.
  *
  * Load the details of the latest ActionFPS article to be displayed in [[controllers.IndexController.index]].
  *
  * @see We use a Apache's Cached HTTP Client.
  *      [[https://hc.apache.org/httpcomponents-client-ga/tutorial/html/caching.html]]
  *
  */
class NewsService(implicit executionContext: ExecutionContext) {
  private val client: CloseableHttpClient =
    CachingHttpClientBuilder.create().build()
  private val context = HttpCacheContext.create()
  //  private val atomUrl = "https://actionfps.blogspot.com/feeds/posts/default"
  private val atomUrl = "https://actionfps.wordpress.com/feed/atom/"

  def latestItem(): NewsItems = {
    val atomContent = EntityUtils.toString(
      client.execute(new HttpGet(atomUrl), context).getEntity)
    NewsService.extractNewsItems(scala.xml.XML.loadString(atomContent))
  }

  def latestItemFuture(): Future[NewsItems] = {
    Future(concurrent.blocking(latestItem()))
  }

}

object NewsService {

  private def firstEntry(xml: scala.xml.Elem): Node = (xml \\ "entry").head

  private def secondEntry(xml: scala.xml.Elem): Option[Node] =
    (xml \\ "entry").drop(1).headOption

  private def extractNewsItem(entry: scala.xml.Node): NewsItem = {
    val link = (entry \ "link")
      .filter(l => (l \ "@rel").text == "alternate")
      .filter(l => (l \ "@type").text == "text/html")
      .head
    val published = ZonedDateTime.parse((entry \ "published").head.text)
    val updated = ZonedDateTime.parse((entry \ "updated").head.text)
    val title = Option((link \ "@title").text)
      .filter(_.nonEmpty)
      .getOrElse((entry \\ "title").take(1).text)
      .trim
    val url = (link \ "@href").text
    NewsItem(postDate = published,
             updateDate = updated,
             title = title,
             url = url)
  }

  def extractNewsItems(xml: Elem): NewsItems = {
    NewsItems(extractNewsItem(firstEntry(xml)),
              secondEntry(xml).map(extractNewsItem))
  }

  case class NewsItems(first: NewsItem, second: Option[NewsItem]) {
    def htmlContent: Html = first.htmlContent(second)
  }

  case class NewsItem(postDate: ZonedDateTime,
                      updateDate: ZonedDateTime,
                      title: String,
                      url: String) {
    private def formattedPostedTime =
      postDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    private def formattedUpdatedTime =
      updateDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    def htmlContent(previous: Option[NewsItem]): Html = {
      Html(
        <div>
          <h3>
            <a href={url} target="_blank">
              {title}
            </a>
          </h3>
          <p>Published:
            <relative-time datetime={formattedPostedTime}>
              {formattedPostedTime}
            </relative-time>{if (!postDate.isEqual(updateDate))
            <span>
              <br/>
              Updated:
              <relative-time datetime={formattedUpdatedTime}>
                {formattedUpdatedTime}
              </relative-time>
            </span>}
          </p>
          {previous.map{p =>
          <div><hr/>
          <p>
          <relative-time datetime={p.formattedPostedTime}>
            {p.formattedPostedTime}</relative-time>:
            <a href={p.url}>{p.title}</a>
          </p></div>
        }.toList}
        </div>.toString
      )
    }
  }

}
