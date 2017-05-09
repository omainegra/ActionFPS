package controllers

import javax.inject._

import lib.WebTemplateRender
import play.api.mvc._

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class ServersController @Inject()(templateRender: WebTemplateRender,
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
          supportsJson = false)(views.html.servers(got)))
    }
  }

}
