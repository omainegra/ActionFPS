package services

import java.io.File
import java.time.Instant
import javax.inject._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Source}
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.{InterOut, IntersIterator}
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

  private implicit val validServers = ValidServers.fromResource

  pickedFile.foreach { f =>
    logger.info(s"Tailing for inters from ${f}...")
    import IntersService.RichInterOut
    FileTailSource
      .lines(f.toPath, maxLineSize = 4096, pollingInterval = 1.second)
      .via(IntersService.lineToEventFlow(
        () => referenceProvider.users.map { users =>
          (nickname: String) => users.find(_.nickname.nickname == nickname).map(_.id)
        },
        () => Instant.now()))
      .map(_.toEvent)
      .runForeach(intersChannel.push)
  }

}

object IntersService {

  private val TimeLeeway = java.time.Duration.ofMinutes(3)

  def lineToEventFlow(usersProvider: () => Future[String => Option[String]],
                      instant: () => Instant)
                     (implicit validServers: ValidServers,
                      executionContext: ExecutionContext): Flow[String, InterOut, NotUsed] = {
    Flow[String]
      .scanAsync(IntersIterator.empty) {
        case (a, b) => async {
          a.accept(b)(await(usersProvider()))
        }
      }
      .mapConcat(_.interOut.toList)
      .filter {
        msg =>

          /** Give a 3 minute time skew leeway **/
          msg.instant.plus(TimeLeeway).isAfter(instant())
      }
  }

  implicit class RichInterOut(interOut: InterOut) {

    import interOut._

    def toEvent: Event = {
      Event(
        id = Option(instant.toString),
        name = Option("inter"),
        data = Json.toJson(Map(
          "playerName" -> playerName,
          "serverName" -> serverName,
          "serverConnect" -> serverConnect
        )).toString
      )
    }
  }

}
