package services

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import akka.actor.ActorSystem
import lib.CallbackTailer
import play.api.libs.iteratee.Concurrent
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 28/12/2015.
  */
@Singleton
class EventsListener @Inject()(configuration: Configuration,
                               recordsService: RecordsService,
                               applicationLifecycle: ApplicationLifecycle,
                               gamesService: GamesService,
                               newGamesService: NewGamesService)
                              (implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  val logger = Logger(getClass)
  logger.info("Started")

  case class EE(data: String, id: Option[String], name: Option[String])

  implicit val eventJson = Json.reads[EE].map { case EE(d, i, n) => EventSource.Event(d, i, n) }

  val (intersEnum, intersChannel) = {
    val broadcast@(enum, chan) = Concurrent.broadcast[Event]
    import concurrent.duration._
    val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(chan.push(Event("")))
    applicationLifecycle.addStopHook(() => Future.successful(keepAlive.cancel()))
    broadcast
  }

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
            newGamesService.accept(game.get)
            gamesService.processGame(game.get)
          }
          if (e.name.contains("inter")) {
            intersChannel.push(e)
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
