package services

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorSystem, Kill, Props}
import akka.agent.Agent
import akka.stream.scaladsl.Source
import com.actionfps.gameparser.Maps
import com.actionfps.pinger._
import controllers.Common
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.streams.Streams
import providers.ReferenceProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PingerService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              referenceProvider: ReferenceProvider
                             )(implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]
  val liveGamesSource = Source.fromPublisher(Streams.enumeratorToPublisher(liveGamesEnum))

  import concurrent.duration._

  val keepAlive = actorSystem.scheduler.schedule(1.second, 5.seconds)(liveGamesChan.push(Event("")))

  applicationLifecycle.addStopHook(() => Future.successful(keepAlive.cancel()))

  implicit val spw = Json.writes[ServerPlayer]
  implicit val stw = Json.writes[ServerTeam]
  implicit val cgw = Json.writes[CurrentGame]
  implicit val ssw = Json.writes[ServerStatus]
  implicit val cgpw = Json.writes[CurrentGamePlayer]
  implicit val cgtw = Json.writes[CurrentGameTeam]
  implicit val cgnsw = Json.writes[CurrentGameNowServer]
  implicit val cgnw = Json.writes[CurrentGameNow]
  implicit val cgsw = Json.writes[CurrentGameStatus]

  val status = Agent(Map.empty[String, Event])
  val listenerActor = actor(factory = actorSystem, name = "pinger")(new PingerService.ListenerActor({
    a =>
      liveGamesChan.push(
        Event(
          id = Option(a.server),
          name = Option("server-status"),
          data = Json.toJson(a).toString()
        ))

  }, { b =>

    val cgse =
      Event(
        id = Option(b.now.server.server),
        name = Option("current-game-status"),
        data = Json.toJson(b).toString()
      )
    liveGamesChan.push(cgse)

    status.alter(m => m.updated(s"${cgse.id}${cgse.name}", cgse))

    val body = views.html.rendergame.live.apply(b, Maps.resource.maps.mapValues(_.image))
    val event = Event(
      id = Option(b.now.server.server),
      name = Option("current-game-status-fragment"),
      data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(body.toString())).toString()
    )
    liveGamesChan.push(event)
    status.alter(m => m.updated(s"${event.id}${event.name}", event))
  }))

  import concurrent.duration._

  val schedule = actorSystem.scheduler.schedule(0.seconds, 5.seconds) {
    referenceProvider.servers.foreach(_.foreach { server =>
      listenerActor ! SendPings(server.hostname, server.port)
    })
  }

  applicationLifecycle.addStopHook(() => Future.successful(listenerActor ! Kill))
  applicationLifecycle.addStopHook(() => Future.successful(schedule.cancel()))


}

object PingerService {


  object ListenerActor {
    def props(g: ServerStatus => Unit, h: CurrentGameStatus => Unit) = Props(new ListenerActor(g, h))
  }

  class ListenerActor(g: ServerStatus => Unit, h: CurrentGameStatus => Unit) extends Act with ActorLogging {

    log.info("Starting listener actor for pinger service...")

    val pingerActor = context.actorOf(name = "pinger", props = Pinger.props)

    become {
      case sp: SendPings =>
        pingerActor ! sp
      case a: ServerStatus =>
        g(a)
      case b: CurrentGameStatus =>
        h(b)
    }
  }

}
