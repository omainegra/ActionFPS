package services

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import akka.NotUsed
import akka.actor.ActorDSL._
import akka.actor.{ActorSystem, Kill}
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.actionfps.pinger._
import com.actionfps.reference.Maps
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.streams.Streams
import providers.ReferenceProvider

import scala.concurrent.{ExecutionContext, Future}
import concurrent.duration._

/**
  * Pings live ActionFPS Servers using the <a href="https://github.com/ActionFPS/server-pinger">Server Pinger library</a>.
  * Provides the result to [[controllers.LiveGamesController]]
  *
  * @todo Clean it up, it's very ugly right now.
  */
@Singleton
class PingerService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              referenceProvider: ReferenceProvider
                             )(implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  implicit val actorMaterializer = ActorMaterializer()

  private val logger = Logger(getClass)

  private val (liveGamesEnum, liveGamesChan) = Concurrent.broadcast[Event]

  val liveGamesSource = Source.fromPublisher(Streams.enumeratorToPublisher(liveGamesEnum))

  private val status = Agent(Map.empty[String, Event])

  val liveGamesWithRetainedSource: Source[Event, NotUsed] = Source(iterable = status.get().valuesIterator.toList)
    .concat(liveGamesSource)

  private val listenerActor = actor(factory = actorSystem, name = "pinger")(new ListenerActor({
    a =>
      liveGamesChan.push(
        Event(
          id = Option(a.server),
          name = Option("server-status"),
          data = Json.toJson(a).toString()
        ))

  }, { be =>
    referenceProvider.Users().users.foreach { lu =>
      val b = be.withUsers(lu.map(u => u.nickname.nickname -> u.id).toMap.get)

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
    }
  }))

  Source
    .tick(0.seconds, 5.seconds, referenceProvider)
    .mapAsync(1)(_.servers)
    .mapConcat(identity)
    .mapMaterializedValue { can => applicationLifecycle.addStopHook(() => Future.successful(can.cancel())) }
      .map{ server => SendPings(server.hostname, server.port)}
    .runForeach(listenerActor.!)

  applicationLifecycle.addStopHook(() => Future.successful(listenerActor ! Kill))

}
