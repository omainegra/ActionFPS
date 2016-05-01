package controllers

/**
  * Created by William on 05/12/2015.
  */

import javax.inject._
import com.actionfps.accumulation.FullIterator
import play.api.Configuration
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider

@Singleton
class Admin @Inject()(fullProvider: FullProvider, referenceProvider: ReferenceProvider, configuration: Configuration)
  extends Controller {
  def reloadReference = Action { request =>
    val apiKeyO = request.getQueryString("api-key").orElse(request.headers.get("api-key"))
    val apiKeyCO = configuration.getString("af.admin-api-key")
    if (apiKeyO == apiKeyCO) {
      referenceProvider.unCache()
      fullProvider.reloadReference()
      Ok("Done reloading")
    } else {
      Forbidden("Wrong api key.")
    }
  }
}
