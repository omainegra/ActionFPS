package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import com.actionfps.stats.Stats
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import play.twirl.api.Html
import providers.ReferenceProvider
import providers.full.FullProvider

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

/**
  * Created by me on 22/04/2016.
  */
class StatsController @Inject()(common: Common,
                                referenceProvider: ReferenceProvider,
                                fullProvider: FullProvider)
                               (implicit configuration: Configuration,
                                executionContext: ExecutionContext,
                                wSClient: WSClient) extends Controller {

  def stats: Action[AnyContent] = Action.async { implicit request =>
    import scala.async.Async._

    async {
      val gc = await(fullProvider.allGames).map(_.id).foldLeft(Stats.GameCounter.empty)(_.include(_))
        .take(366)
      val gcHtml = views.html.stats.gameCounter(gc)
//      val pcHtml = views.html.punch_card(gc.punchCard)
      Ok(common.renderTemplate(
        title = Option("Stats"),
        supportsJson = false,
        login = None
      )(
        html = views.html.stats.tableau.apply()
      ))
    }
  }

}

object StatsController {
  val fmt = DateTimeFormatter.ISO_DATE
  implicit val lmWriter: Writes[ListMap[ZonedDateTime, Int]] = Writes[ListMap[ZonedDateTime, Int]](pgc =>
    JsArray(pgc.map {
      case (d, n) => JsObject(Map(
        "date" -> JsString(fmt.format(d)),
        "count" -> JsNumber(n)
      ))
    }.toList)
  )
}
