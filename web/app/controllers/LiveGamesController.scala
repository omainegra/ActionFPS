package controllers

import javax.inject.Inject

import lib.KeepAliveEvents
import play.api.mvc.{Action, Controller}
import services.PingerService

/**
  * Created by me on 30/08/2016.
  */
class LiveGamesController @Inject()(pingerService: PingerService) extends Controller {
  def serverUpdates = Action {
    Ok.chunked(
      content = pingerService.liveGamesWithRetainedSource.merge(KeepAliveEvents.source)
    ).as("text/event-stream")
  }
}
