package controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import services.{PingerService, IntersService, NewGamesService}

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class LiveUpdatesController @Inject()(pingerService: PingerService,
                                      intersService: IntersService,
                                      newGamesService: NewGamesService) extends Controller {

  def serverUpdates = Action {
    Ok.feed(
      content = pingerService.liveGamesEnum
    ).as("text/event-stream")
  }

  def inters = Action {
    Ok.feed(
      content = intersService.intersEnum
    ).as("text/event-stream")
  }

  def newGames = Action {
    Ok.feed(
      content = newGamesService.newGamesEnum
    ).as("text/event-stream")
  }
}
