package controllers

import lib.WebTemplateRender
import play.api.Configuration
import play.api.mvc._

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */
class ServersController(templateRender: WebTemplateRender,
                        configuration: Configuration,
                        providesServers: ProvidesServers,
                        components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components) {

  def servers: Action[AnyContent] = Action.async { implicit request =>
    async {
      val got = await(providesServers.servers)
      Ok(
        templateRender.renderTemplate(
          title = Some("ActionFPS Servers"),
          jsonLink = Some(configuration.underlying.getString(
            ServersController.ServersReferenceUrlConfigurationKey))
        )(views.html.servers(got)))
    }
  }

}
object ServersController {
  val ServersReferenceUrlConfigurationKey = "af.reference.servers"
}
