package controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import services.{PingerService}

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class LiveUpdatesController @Inject()(pingerService: PingerService) extends Controller {

  def serverUpdates = Action {
    Ok.feed(
      content = pingerService.liveGamesEnum
    ).as("text/event-stream")
  }

  def inters = TODO

  def newGames = TODO
//  Action {
//    Ok.feed(
//      content = newGamesService.newGamesEnum
//    ).as("text/event-stream")
//  }
}
