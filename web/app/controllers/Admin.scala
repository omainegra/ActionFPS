package controllers

/**
  * Created by William on 05/12/2015.
  */
import javax.inject._

import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import providers.ReferenceProvider
import providers.full.FullProvider

@Singleton
class Admin @Inject()(fullProvider: FullProvider,
                      referenceProvider: ReferenceProvider,
                      configuration: Configuration,
                      components: ControllerComponents)
    extends AbstractController(components) {

  /**
    * Reloads reference data and forces [[FullProvider]] to reevaluate usernames and clans for all games.
    *
    * @return
    */
  def reloadReference = Action { request =>
    val apiKeyO =
      request.getQueryString("api-key").orElse(request.headers.get("api-key"))
    val apiKeyCO = configuration.getOptional[String]("af.admin-api-key")
    if (apiKeyO == apiKeyCO) {
      referenceProvider.unCache()
      Ok("Done reloading")
    } else {
      Forbidden("Wrong api key.")
    }
  }
}
