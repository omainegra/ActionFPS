package services

import javax.inject._

import acleague.pinger._
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorSystem, Kill, Props}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 07/12/2015.
  */
@Singleton
class PingerService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              recordsService: RecordsService
//                              ,
//                              gameRenderService: GameRenderService
                             )(implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]

  implicit val spw = Json.writes[ServerPlayer]
  implicit val stw = Json.writes[ServerTeam]
  implicit val cgw = Json.writes[CurrentGame]
  implicit val ssw = Json.writes[ServerStatus]
  implicit val cgpw = Json.writes[CurrentGamePlayer]
  implicit val cgtw = Json.writes[CurrentGameTeam]
  implicit val cgnsw = Json.writes[CurrentGameNowServer]
  implicit val cgnw = Json.writes[CurrentGameNow]
  implicit val cgsw = Json.writes[CurrentGameStatus]
  val listenerActor = actor(factory = actorSystem, name = "pinger")(new PingerService.ListenerActor({
    a =>
      liveGamesChan.push(
        Event(
          id = Option(a.server),
          name = Option("server-status"),
          data = Json.toJson(a).toString()
        ))

  }, { b =>
    liveGamesChan.push(
      Event(
        id = Option(b.now.server.server),
        name = Option("current-game-status"),
        data = Json.toJson(b).toString()
      )
    )
    val html = controllers.jsonToHtml("/live/render-fragment.php", Json.toJson(b))
    liveGamesChan.push(
      Event(
        id = Option(b.now.server.server),
        name = Option("current-game-status-fragment"),
        data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(html.body)).toString()
      )
    )
  }))

  import concurrent.duration._

  val schedule = actorSystem.scheduler.schedule(0.seconds, 5.seconds) {
    recordsService.servers.foreach { server =>
      listenerActor ! SendPings(server.hostname, server.port)
    }
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