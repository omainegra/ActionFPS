package controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import services.{EventPublisherService, IntersService, NewGamesService}

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class EventsController @Inject()(eventPublisherService: EventPublisherService,
                                 newGamesService: NewGamesService) extends Controller {
  def events = Action {
    Ok.feed(
      content = eventPublisherService.eventsEnum
    ).as("text/event-stream")
  }

}
