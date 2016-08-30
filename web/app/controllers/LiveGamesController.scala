package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Source
import play.api.mvc.{Action, Controller}
import services.PingerService

/**
  * Created by me on 30/08/2016.
  */
class LiveGamesController @Inject()(pingerService: PingerService) extends Controller {

  def serverUpdates = Action {
    Ok.chunked(
      content = {
        Source(iterable = pingerService.status.get().valuesIterator.toList)
          .concat(pingerService.liveGamesSource)
      }
    ).as("text/event-stream")
  }
}
