package controllers

import javax.inject.Inject

import lib.KeepAliveEvents
import play.api.mvc.{Action, Controller}
import providers.games.NewGamesProvider
import services.{IntersService, PingerService}

class EventStream @Inject()(pingerService: PingerService,
                            intersService: IntersService,
                            newGamesProvider: NewGamesProvider) extends Controller {
  def eventStream() = Action {
    Ok.chunked(
      content = pingerService
        .liveGamesSource
        .merge(newGamesProvider.newGamesSource)
        .merge(intersService.intersSource)
        .merge(KeepAliveEvents.source)
    )
  }
}
