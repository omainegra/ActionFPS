package services

import java.io.File
import java.time.ZonedDateTime
import javax.inject._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Source, _}
import com.actionfps.accumulation.ValidServers
import com.actionfps.gameparser.mserver.ExtractMessage
import com.actionfps.inter.{InterMessage, InterState}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json
import play.api.libs.streams.Streams
import play.api.{Configuration, Logger}
import providers.ReferenceProvider

import scala.async.Async._
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
@Singleton
class IntersService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              referenceProvider: ReferenceProvider,
                              configuration: Configuration
                             )(implicit
                               actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  private val logger = Logger(getClass)

  implicit val actorMaterializer = ActorMaterializer()

  private val (intersEnum, intersChannel) = Concurrent.broadcast[Event]

  def intersSource: Source[Event, NotUsed] = Source.fromPublisher(Streams.enumeratorToPublisher(intersEnum))

  private val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(intersChannel.push(Event("")))
  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))

  private val pickedFile = {
    configuration
      .underlying
      .getStringList("af.journal.paths")
      .asScala
      .map(new File(_))
      .toList
      .filter(_.exists())
      .sortBy(_.lastModified())
      .lastOption
  }

  private val interStateAgent = Agent(InterState.empty)

  private val validServers = ValidServers.fromResource

  pickedFile.foreach { f =>
    logger.info(s"Tailing for inters from ${f}...")
    FileTailSource
      .lines(f.toPath, maxLineSize = 4096, pollingInterval = 1.second)
      .collect {
        case ExtractMessage(zdt, validServers.FromLog(server), InterMessage(interMessage))
          if server.address.isDefined => (server, zdt, interMessage.toCall(
          time = zdt,
          server = server.name
        ))
      }.mapAsync(1) { case (server, zdt, interCall) =>
      async {
        val allowed = {
          val userIsRegistered = await(referenceProvider.users).exists(_.nickname.nickname == interCall.nickname)
          val isNotExpired = interStateAgent.get().canAdd(interCall)
          val inLastFiveMinutes = zdt.isAfter(ZonedDateTime.now().minusMinutes(10))
          userIsRegistered && isNotExpired && inLastFiveMinutes
        }
        (server, zdt, interCall, allowed)
      }
    }.mapAsync(1) { case (server, zdt, interCall, allowed) =>
      interStateAgent.alter { oldState =>
        logger.debug(s"Received $interCall. Allowed? $allowed")
        if (allowed) {
          intersChannel.push(Event(
            id = Option(interCall.time.toString),
            name = Option("inter"),
            data = Json.toJson(Map(
              "playerName" -> interCall.nickname,
              "serverName" -> server.name,
              "serverConnect" -> server.address.get
            )).toString
          ))
          oldState + interCall
        } else oldState
      }
    }
      .to(Sink.ignore)
      .run()
  }

}
