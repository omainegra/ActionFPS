package services

import javax.inject._

import akka.actor.ActorDSL._
import acleague.pinger._
import akka.actor.{Props, Kill, ActorSystem}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 07/12/2015.
  */
@Singleton
class PingerService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              recordsService: RecordsService
                             )(implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]

  val listenerActor = actor(factory = actorSystem, name = "pinger")(new PingerService.ListenerActor({ e =>
    liveGamesChan.push(e)
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

  implicit val spw = Json.writes[ServerPlayer]
  implicit val stw = Json.writes[ServerTeam]
  implicit val cgw = Json.writes[CurrentGame]
  implicit val ssw = Json.writes[ServerStatus]
  implicit val cgpw = Json.writes[CurrentGamePlayer]
  implicit val cgtw = Json.writes[CurrentGameTeam]
  implicit val cgnsw = Json.writes[CurrentGameNowServer]
  implicit val cgnw = Json.writes[CurrentGameNow]
  implicit val cgsw = Json.writes[CurrentGameStatus]

  object ListenerActor {
    def props(g: Event => Unit) = Props(new ListenerActor(g))
  }

  class ListenerActor(g: Event => Unit) extends Act {

    val pingerActor = context.actorOf(name = "pinger", props = Pinger.props)

    become {
      case sp: SendPings =>
        pingerActor ! sp
      case a: ServerStatus =>
        g(Event(
          id = Option(a.server),
          name = Option("server-status"),
          data = Json.toJson(a).toString()
        ))
      case b: CurrentGameStatus =>
        g(Event(
          id = Option(b.now.server.server),
          name = Option("current-game-status"),
          data = Json.toJson(b).toString()
        ))
    }
  }

}