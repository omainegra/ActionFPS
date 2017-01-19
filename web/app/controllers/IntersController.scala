package controllers

import javax.inject.{Inject, Singleton}

import lib.KeepAliveEvents
import play.api.mvc.{Action, Controller}
import services.IntersService

/**
  * Created by William on 13/01/2016.
  */

@Singleton
class IntersController @Inject()(intersService: IntersService) extends Controller {

  def inters = Action {
    Ok.chunked(
      content = intersService.intersSource.merge(KeepAliveEvents.source)
    ).as("text/event-stream")
  }

}
