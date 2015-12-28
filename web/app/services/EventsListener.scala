package services

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import lib.CallbackTailer
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.libs.json.Json

import scala.concurrent.Future

/**
  * Created by William on 28/12/2015.
  */
@Singleton
class EventsListener @Inject()(configuration: Configuration,
                               recordsService: RecordsService,
                               applicationLifecycle: ApplicationLifecycle,
                               gamesService: GamesService) {

  val logger = Logger(getClass)
  logger.info("Started")

  case class EE(data: String, id: Option[String], name: Option[String])

  implicit val eventJson = Json.reads[EE].map { case EE(d, i, n) => EventSource.Event(d, i, n) }

  val tailer = new CallbackTailer(new File(configuration.underlying.getString("af.events")), true)((line) => {
    line.split("\t").toList match {
      case List(id, cont) =>
        val event = Json.fromJson[Event](Json.parse(cont))
        event.foreach { e =>
          if ( //e.data == "started up" ||
            e.data == "reference-updated") {
            recordsService.updateSync()
          }
          if (e.name.contains("new-game")) {
            val game = Json.fromJson[JsonGame](Json.parse(e.data))
            gamesService.processGame(game.get)
          }
        }
        println(s"received event = ${id} ==> ${event}")
    }
  })

  applicationLifecycle.addStopHook(() => Future.successful {
    tailer.shutdown()
    logger.info("Shut down")
  })

}
