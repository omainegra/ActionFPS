package controllers

import javax.inject._

import lib.WebTemplateRender
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class ServersController @Inject()(templateRender: WebTemplateRender,
                                  providesServers: ProvidesServers)(
    implicit executionContext: ExecutionContext)
    extends Controller {

  import templateRender._

  def servers: Action[AnyContent] = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("json") =>
          Ok(Json.toJson(await(providesServers.servers)))
        case _ =>
          val got = await(providesServers.servers)
          Ok(
            renderTemplate(title = Some("ActionFPS Servers"),
                           supportsJson = true)(views.html.servers(got)))
      }
    }
  }

}
