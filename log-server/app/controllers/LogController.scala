package controllers

import java.io.FileInputStream
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import af.FileOffsetFinder
import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Source, StreamConverters}
import akka.util.ByteString
import com.actionfps.gameparser.mserver.ExtractMessage
import controllers.LogController._
import pdi.jwt.JwtSession._
import pdi.jwt._
import play.api.Logger
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import scala.concurrent.duration._

abstract class LogController(sourceFile: Path,
                             components: ControllerComponents)
    extends AbstractController(components) {
  Logger.info(s"Log controller for ${sourceFile}")
  def stream = Action { request =>
    if (!request.jwtSession.isEmpty()) {
      require(request.jwtSession.claim.isValid(IssuerName))
    }
    val logAccess: LogAccess = request.jwtSession.claimData
      .asOpt[LogAccess]
      .getOrElse(LogAccess.default)
    val now = ZonedDateTime.now()
    val startTime = request.headers
      .get(LastEventIdHeader)
      .map(ZonedDateTime.parse)
      .getOrElse(now)
      .toInstant

    val outsideOfRange = {
      startTime
        .atZone(ZoneId.systemDefault())
        .isBefore(
          ZonedDateTime
            .now()
            .minus(LogController.OldLogsPeriod))
    }

    if (!logAccess.readOld && outsideOfRange)
      Unauthorized(
        s"We only allow ${LogController.OldLogsPeriod} period of access without keys.\n")
    else {
      val fileOffset =
        FileOffsetFinder(startTime.toString).apply(sourceFile)
      val messagesStream = FileTailSource
        .apply(
          path = sourceFile,
          maxChunkSize = 8096,
          pollingInterval = 1.second,
          startingPosition = fileOffset
        )
        .via(
          akka.stream.scaladsl.Framing
            .delimiter(ByteString.fromString("\n"),
                       8096,
                       allowTruncation = false))
        .map(_.decodeString("UTF-8"))
        .collect[MessageType] {
          case ExtractMessage(zdt, server, message) => (zdt, server, message)
        }
        .dropWhile(_._1.toInstant.isBefore(startTime))

      val eventsStream = messagesStream
        .via(logAccess.filterFlow)
        .map[Event] {
          case (zdt, server, message) =>
            Event(
              id = Some(DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant)),
              data = s"${DateTimeFormatter.ISO_INSTANT
                .format(zdt.toInstant)}\t${server}\t${message}\n",
              name = Some("log")
            )
        }
        .merge(keepAliveEventSource)
      Ok.chunked(eventsStream).as(ContentTypes.EVENT_STREAM)
    }
  }

  def historical(from: String, to: Option[String]) = Action { request =>
    if (!request.jwtSession.isEmpty()) {
      require(request.jwtSession.claim.isValid(IssuerName))
    }
    val logAccess: LogAccess = request.jwtSession.claimData
      .asOpt[LogAccess]
      .getOrElse(LogAccess.default)

    val fromTime = ZonedDateTime.parse(from)
    val outsideOfRange = {
      fromTime
        .isBefore(
          ZonedDateTime
            .now()
            .minus(LogController.OldLogsPeriod))
    }

    if (!logAccess.readOld && outsideOfRange)
      Unauthorized(
        s"We only allow ${LogController.OldLogsPeriod} period of access without keys.\n")
    else {
      val fileOffset =
        FileOffsetFinder(fromTime.toInstant.toString).apply(sourceFile)
      val fileSource = StreamConverters
        .fromInputStream(() => {
          val fis = new FileInputStream(sourceFile.toFile)
          fis.skip(fileOffset)
          fis
        })
        .via(
          akka.stream.scaladsl.Framing
            .delimiter(ByteString.fromString("\n"),
                       8096,
                       allowTruncation = false))
        .map(_.decodeString("UTF-8"))

      val toTime = to.map(ZonedDateTime.parse).getOrElse(ZonedDateTime.now())

      val dataSource = fileSource
        .collect[MessageType] {
          case ExtractMessage(zdt, server, message) => (zdt, server, message)
        }
        .dropWhile(_._1.isBefore(fromTime))
        .takeWhile(_._1.isBefore(toTime))
        .via(logAccess.filterFlow)
        .map {
          case (zdt, server, message) =>
            s"${DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant)}\t${server}\t${message}\n"
        }
      Ok.chunked(dataSource).as(LogController.TsvMimeType)
    }
  }

}

object LogController {

  val OldLogsPeriod: java.time.Period = java.time.Period.ofMonths(3)

  val IssuerName = "af"

  val fromQueryString = "from"
  val toQueryString = "to"

  val LastEventIdHeader = "Last-Event-ID"

  val TsvMimeType = "text/tab-separated-values"

  /** Needed to prevent premature close of connection if not enough events coming through **/
  val keepAliveEventSource: Source[Event, Cancellable] = {
    import concurrent.duration._
    Source.tick(10.seconds, 10.seconds, Event(""))
  }

  def filterOutIp(message: String): String = {

    val matchingRegex = """^\[[0-9\.]+(.*)$"""
    val matchingRegexOther = """^(.*)(normal|admin)([ ]+)[0-9\.]+(.*)$"""

    message
      .replaceFirst(matchingRegex, "[0.0.0.0$1")
      .replaceFirst(matchingRegexOther, "$1$2$30.0.0.0$4")

  }

  def ignorePrivateConversation(message: String): Boolean = {
    val matchingRegex = """^[^ ]+ [^ ]+ says to [^ :]+: .*$"""
    message.matches(matchingRegex)
  }

  type MessageType = (ZonedDateTime, String, String)

  case class LogAccess(levels: Set[String]) {
    def showIps: Boolean = levels.contains("ip")

    def readOld: Boolean = levels.contains("old")

    def filterFlow: Flow[MessageType, MessageType, NotUsed] = {
      Flow[MessageType]
        .map {
          case (z, s, m) =>
            (z, s, if (showIps) m else LogController.filterOutIp(m))
        }
        .filterNot {
          case (a, b, c) => LogController.ignorePrivateConversation(c)
        }
    }
  }
  object LogAccess {
    val default: LogAccess = LogAccess(levels = Set.empty)
    implicit val formats: OFormat[LogAccess] = Json.format[LogAccess]
  }

  def issueJwt(key: String,
               logAccess: LogAccess,
               expireSeconds: Long): String = {
    import pdi.jwt.{JwtAlgorithm, JwtJson}
    import play.api.libs.json.Json
    val algo = JwtAlgorithm.HS256
    JwtJson.encode(
      header = JwtHeader(JwtAlgorithm.HS256),
      claim = JwtClaim(issuer = Some(IssuerName),
                       content = Json.toJson(logAccess).toString).issuedNow
        .expiresIn(expireSeconds),
      key
    )
  }

  /** To issue, do:
  * $ sbt logServer/console
  * ...
  * scala> issueJwt(key = "LOLOL", logAccess = LogAccess(Set("ip")), expireSeconds = 200)
  * ey.ab.cd
  */

}
