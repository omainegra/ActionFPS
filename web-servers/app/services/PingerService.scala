package services

/**
  * Created by William on 01/01/2016.
  */
import akka.NotUsed
import akka.actor.ActorDSL._
import akka.actor.{ActorSystem, Kill}
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.actionfps.accumulation.Maps
import com.actionfps.pinger._
import controllers.{ProvidesServers, ProvidesUsersList}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.json.{JsObject, JsString, Json}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Pings live ActionFPS Servers using the <a href="https://github.com/ActionFPS/server-pinger">Server Pinger library</a>.
  *
  * @todo Clean it up, it's very ugly right now.
  */
class PingerService(applicationLifecycle: ApplicationLifecycle,
                    providesServers: ProvidesServers,
                    providesUsers: ProvidesUsersList)(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext) {

  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  private val logger = Logger(getClass)

  private val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]

  private val liveGamesSource =
    Source.fromPublisher(IterateeStreams.enumeratorToPublisher(liveGamesEnum))

  private val status = Agent(Map.empty[String, Event])

  def liveGamesWithRetainedSource: Source[Event, NotUsed] =
    Source(iterable = status.get().valuesIterator.toList)
      .concat(liveGamesSource)

  private val cgsStatus = Agent(
    Map.empty[CurrentGameNowServer, CurrentGameStatus])

  def statusSimilar(a: CurrentGameStatus, b: CurrentGameStatus): Boolean = {
    a.copy(updatedTime = "", when = "") == b.copy(updatedTime = "", when = "")
  }

  private val ssStatus = Agent(Map.empty[String, ServerStatus])

  def serverStatusSimilar(a: ServerStatus, b: ServerStatus): Boolean = {
    a.copy(updatedTime = "") == b.copy(updatedTime = "")
  }

  private val listenerActor = actor(factory = actorSystem, name = "pinger")(
    new ListenerActor(
      { a =>
        if (!ssStatus.get().contains(a.server) || !ssStatus
              .get()
              .get(a.server)
              .exists(serverStatusSimilar(_, a))) {
          ssStatus.send(_.updated(a.server, a))
          liveGamesChan.push(
            Event(
              id = Option(a.server),
              name = Option("server-status"),
              data = Json.toJson(a).toString()
            ))
        }
      }, { be =>
        if (!cgsStatus.get().contains(be.now.server) || !cgsStatus
              .get()
              .get(be.now.server)
              .exists(statusSimilar(be, _))) {
          cgsStatus.send(_.updated(be.now.server, be))
          providesUsers.users.foreach {
            lu =>
              val b = be.withUsers(
                lu.map(u => u.nickname.nickname -> u.id).toMap.get)

              val cgse =
                Event(
                  id = Option(b.now.server.server),
                  name = Option("current-game-status"),
                  data = Json.toJson(b).toString()
                )
              liveGamesChan.push(cgse)

              status.alter(m => m.updated(s"${cgse.id}${cgse.name}", cgse))

              providesServers.servers.foreach {
                servers =>
                  val body =
                    try {
                      views.rendergame.Live.render(b, Maps.mapToImage, servers)
                    } catch {
                      case e: Throwable =>
                        Logger.error(s"Failed to render game ${b}", e)
                    }
                  val event = Event(
                    id = Option(b.now.server.server),
                    name = Option("current-game-status-fragment"),
                    data = Json
                      .toJson(b)
                      .asInstanceOf[JsObject]
                      .+("html" -> JsString(body.toString()))
                      .toString()
                  )
                  liveGamesChan.push(event)
                  status.alter(m =>
                    m.updated(s"${event.id}${event.name}", event))
              }
          }
        }
      }
    ))

  Source
    .tick(0.seconds, 5.seconds, providesServers)
    .mapAsync(1)(_.servers)
    .mapConcat(identity)
    .mapMaterializedValue { can =>
      applicationLifecycle.addStopHook(() => Future.successful(can.cancel()))
    }
    .map { server =>
      SendPings(server.hostname, server.port)
    }
    .runForeach(listenerActor.!)

  applicationLifecycle.addStopHook(() =>
    Future.successful(listenerActor ! Kill))

}
