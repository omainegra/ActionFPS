package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import services.IntersService

/**
  * Created by William on 13/01/2016.
  */

class IntersController @Inject()(intersService: IntersService) extends Controller {

  def inters = Action {
    Ok.chunked(
      content = intersService.intersSource
    ).as("text/event-stream")
  }

}
