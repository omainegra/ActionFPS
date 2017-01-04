package services

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import akka.actor.ActorDSL._
import akka.actor.SupervisorStrategy.Decider
import akka.actor.{ActorKilledException, ActorLogging, ActorSystem, Kill, Props, SupervisorStrategy}
import akka.agent.Agent
import akka.stream.scaladsl.Source
import com.actionfps.gameparser.Maps
import com.actionfps.pinger._
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

  private val logger = Logger(getClass)

  private val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]
  val liveGamesSource = Source.fromPublisher(Streams.enumeratorToPublisher(liveGamesEnum))

  import concurrent.duration._

  private val keepAlive = actorSystem.scheduler.schedule(1.second, 5.seconds)(liveGamesChan.push(Event("")))

  applicationLifecycle.addStopHook(() => Future.successful(keepAlive.cancel()))

  implicit private val spw = Json.writes[ServerPlayer]
  implicit private val stw = Json.writes[ServerTeam]
  implicit private val cgw = Json.writes[CurrentGame]
  implicit private val ssw = Json.writes[ServerStatus]
  implicit private val cgpw = Json.writes[CurrentGamePlayer]
  implicit private val cgps = Json.writes[CurrentGameSpectator]
  implicit private val cgpd = Json.writes[CurrentGameDmPlayer]
  implicit private val cgtw = Json.writes[CurrentGameTeam]
  implicit private val cgnsw = Json.writes[CurrentGameNowServer]
  implicit private val cgnw = Json.writes[CurrentGameNow]
  implicit private val cgsw = Json.writes[CurrentGameStatus]

  val status = Agent(Map.empty[String, Event])
  private val usersProvider = referenceProvider.Users().provider
  private val listenerActor = actor(factory = actorSystem, name = "pinger")(new PingerService.ListenerActor({
    a =>
      liveGamesChan.push(
        Event(
          id = Option(a.server),
          name = Option("server-status"),
          data = Json.toJson(a).toString()
        ))

  }, { be =>
    val b = usersProvider.value.flatMap(_.toOption) match {
      case Some(u) => be.withUsers(u.username)
      case None => be
    }

    val cgse =
      Event(
        id = Option(b.now.server.server),
        name = Option("current-game-status"),
        data = Json.toJson(b).toString()
      )
    liveGamesChan.push(cgse)

    status.alter(m => m.updated(s"${cgse.id}${cgse.name}", cgse))


    val body =
      try {
        views.rendergame.Live.render(b, Maps.mapToImage)
      } catch {
        case e: Throwable => Logger.error(s"Failed to render game ${b}", e)
      }
    val event = Event(
      id = Option(b.now.server.server),
      name = Option("current-game-status-fragment"),
      data = Json.toJson(b).asInstanceOf[JsObject].+("html" -> JsString(body.toString())).toString()
    )
    liveGamesChan.push(event)
    status.alter(m => m.updated(s"${event.id}${event.name}", event))
  }))

  import concurrent.duration._

  private val schedule = actorSystem.scheduler.schedule(0.seconds, 5.seconds) {
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

    private val pingerActor = context.actorOf(name = "pinger", props = Pinger.props)

    override final val supervisorStrategy: SupervisorStrategy = {
      def defaultDecider: Decider = {
        case _: ActorKilledException ⇒ Restart
        case _: Exception ⇒ Restart
      }

      OneForOneStrategy()(defaultDecider)
    }

    import concurrent.duration._
    import context.dispatcher

    context.system.scheduler.schedule(10.minutes, 10.minutes, pingerActor, Kill)

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
