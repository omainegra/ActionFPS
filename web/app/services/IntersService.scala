package services

import java.io.File
import java.time.Instant
import javax.inject._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import com.actionfps.accumulation.ValidServers
import com.actionfps.gameparser.mserver.ExtractMessage
import com.actionfps.inter.{InterOut, IntersIterator, UserMessage}
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
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

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
    val startInstant = Instant.now()
    FileTailSource
      .lines(f.toPath, maxLineSize = 4096, pollingInterval = 1.second)
      .filter { case ExtractMessage(zdt, _, _) => zdt.toInstant.isAfter(startInstant) }
      .via(IntersService.lineToEventFlow(
        () => referenceProvider.users.map { users =>
          (nickname: String) => users.find(_.nickname.nickname == nickname).map(_.id)
        },
        () => Instant.now()))
      .withAttributes(ActorAttributes.supervisionStrategy {
        case NonFatal(e) =>
          logger.error(s"Failed an element due to ${e}", e)
          Supervision.Resume
      })
      .mapConcat(_.toEvent.toList)
      .runForeach(intersChannel.push)
      .onComplete { case Success(_) =>
        logger.info(s"Flow finished.")
      case Failure(reason) =>
        logger.error(s"Failed due to ${reason}", reason)
      }
  }

}

object IntersService {

  private val TimeLeeway = java.time.Duration.ofMinutes(3)

  case class UserMessageFromLine(nicknameToUser: String => Option[String])
                                (implicit validServers: ValidServers) {
    private val regex = """^\[([^\]]+)\] ([^ ]+) says: '(.+)'$""".r

    def unapply(line: String): Option[UserMessage] = {
      PartialFunction.condOpt(line) {
        case ExtractMessage(zdt, validServers.FromLog(validServer), regex(ip, nickname, message@"!inter"))
          if nicknameToUser(nickname).isDefined =>
          UserMessage(
            instant = zdt.toInstant,
            serverId = validServer.logId,
            ip = ip,
            nickname = nickname,
            userId = nicknameToUser(nickname).get,
            messageText = message
          )
      }
    }
  }

  def lineToEventFlow(usersProvider: () => Future[String => Option[String]],
                      instant: () => Instant)
                     (implicit validServers: ValidServers,
                      executionContext: ExecutionContext): Flow[String, InterOut, NotUsed] = {
    Flow[String]
      .scanAsync(IntersIterator.empty) {
        case (a, line) => async {
          UserMessageFromLine(await(usersProvider()))
            .unapply(line)
            .flatMap(_.interOut)
            .map(a.acceptInterOut)
            .getOrElse(a.resetInterOut)
        }
      }
      .mapConcat(_.interOut.toList)
      .filter {
        msg =>

          /** Give a 3 minute time skew leeway **/
          msg.userMessage.instant.plus(TimeLeeway).isAfter(instant())
      }
  }

  implicit class RichInterOut(interOut: InterOut) {

    def toEvent(implicit validServers: ValidServers): Option[Event] = {
      for {
        validServer <- validServers.items.get(interOut.userMessage.serverId)
        serverAddress <- validServer.address
      } yield Event(
        id = Option(interOut.userMessage.instant.toString),
        name = Option("inter"),
        data = Json.toJson(Map(
          "playerName" -> interOut.userMessage.nickname,
          "serverName" -> validServer.name,
          "serverConnect" -> serverAddress
        )).toString
      )
    }
  }

}
