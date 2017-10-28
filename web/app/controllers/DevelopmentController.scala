package controllers

import lib.WebTemplateRender
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import scala.concurrent.ExecutionContext

class DevelopmentController(components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components)
    with SimpleRouter {

  def routes: Routes = {
    case GET(p"/development/") =>
      Action {
        Ok.sendFile(
          WebTemplateRender.wwwLocation.resolve("development.html").toFile)
      }
  }
}
