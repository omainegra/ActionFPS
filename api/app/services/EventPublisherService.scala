package services

import java.io.{FileWriter, File}
import java.time.ZonedDateTime
import javax.inject._

import akka.actor.ActorSystem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{Writes, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by William on 28/12/2015.
  */
@Singleton
class EventPublisherService @Inject()(configuration: Configuration,
                                      applicationLifecycle: ApplicationLifecycle)(implicit actorSystem: ActorSystem,
                                                                                  executionContext: ExecutionContext) {

  val (eventsEnum, thing) = Concurrent.broadcast[Event]

  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))

  val file = new File(configuration.underlying.getString("af.events"))
  val fw = new FileWriter(file, true)

  case class EE(data: String, id: Option[String], name: Option[String])

  implicit val ews = Json.writes[EE]

  implicit class eventAdd(event: Event) {
    def toEE = EE(event.data, event.id, event.name)
  }

  def push(event: Event): Unit = {
    val id = ZonedDateTime.now().toString
    val nevent = if (event.id.isEmpty) event.copy(id = Option(id)) else event
    val line = s"""${id}\t${Json.toJson(nevent.toEE)}"""
    thing.push(nevent)
    fw.write(s"$line\n")
    fw.flush()
  }

  applicationLifecycle.addStopHook(() => Future.successful(fw.close()))

  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))

  push(Event("started up"))
}
