package controllers

import javax.inject.{Inject, Singleton}

import com.actionfps.stats.Stats
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import providers.ReferenceProvider
import providers.full.FullProvider

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

  def stats = Action.async { implicit request =>
    import scala.async.Async._
    async {
      Ok(common.renderTemplate(
        title = Option("Stats"),
        supportsJson = false,
        login = None
      )(
        html = Html(
          <div id="stats">
            <table class="listing">
              <tbody>
                {Stats.idsToTableRows(
                ids = await(fullProvider.allGames).map(_.id),
                width = 540,
                height = 20,
                r = 4
              )}
              </tbody>
            </table>
          </div>.toString)
      ))
    }
  }

}
