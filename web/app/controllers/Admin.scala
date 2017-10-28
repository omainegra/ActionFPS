package controllers

/**
  * Created by William on 05/12/2015.
  */
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import providers.{GameAxisAccumulatorProvider, ReferenceProvider}

class Admin(fullProvider: GameAxisAccumulatorProvider,
            referenceProvider: ReferenceProvider,
            configuration: Configuration,
            components: ControllerComponents)
    extends AbstractController(components) {

  /**
    * Reloads reference data and forces [[GameAxisAccumulatorProvider]] to reevaluate usernames and clans for all games.
    */
  def reloadReference = Action { request =>
    val apiKeyO =
      request.getQueryString("api-key").orElse(request.headers.get("api-key"))
    val apiKeyCO = configuration.getOptional[String]("af.admin-api-key")
    if (apiKeyO == apiKeyCO) {
      referenceProvider.unCache()
      fullProvider.ingestUpdatedReference()
      Ok("Done reloading")
    } else {
      Forbidden("Wrong api key.")
    }
  }
}
