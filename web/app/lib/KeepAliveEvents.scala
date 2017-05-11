package lib

import akka.actor.Cancellable
import akka.stream.scaladsl.Source
import play.api.libs.EventSource.Event

import concurrent.duration._

/**
  * Created by me on 19/01/2017.
  */
object KeepAliveEvents {
  val source: Source[Event, Cancellable] =
    Source.tick(10.seconds, 10.seconds, Event(""))
}
