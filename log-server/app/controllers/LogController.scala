package controllers

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{FileIO, Flow, Source}
import akka.util.ByteString
import com.actionfps.gameparser.mserver.ExtractMessage
import controllers.LogController._
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._

import scala.concurrent.duration._

class LogController(sourceFile: Path) extends Controller {

  type MessageType = (ZonedDateTime, String, String)
  def stream = Action { request =>
    val startTime = ZonedDateTime.now()
    val messageFilter: Flow[MessageType, MessageType, NotUsed] =
      request.headers.get(LastEventIdHeader) match {
        case Some(lastId) =>
          Flow[MessageType].dropWhile(
            _._1.isBefore(ZonedDateTime.parse(lastId)))
        case None => Flow[MessageType].dropWhile(_._1.isBefore(startTime))
      }
    val messagesStream = FileTailSource
      .lines(
        path = sourceFile,
        maxLineSize = 8096,
        pollingInterval = 1.second
      )
      .collect[MessageType] {
        case ExtractMessage(zdt, server, message) => (zdt, server, message)
      }
      .via(messageFilter)
      .map { case (z, s, m) => (z, s, LogController.filterOutIp(m)) }
      .filterNot {
        case (a, b, c) => LogController.ignorePrivateConversation(c)
      }

    val eventsStream = messagesStream
      .map[Event] {
        case (zdt, server, message) =>
          Event(
            id = Some(DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant)),
            data = message,
            name = Some(server)
          )
      }
      .merge(keepAliveEventSource)
    Ok.chunked(eventsStream).as(ContentTypes.EVENT_STREAM)
  }

  def historical(from: String, to: String) = Action { request =>
    val fileSource = FileIO
      .fromPath(
        f = sourceFile
      )
      .via(akka.stream.scaladsl.Framing
        .delimiter(ByteString.fromString("\n"), 8096, allowTruncation = false))
      .map(_.decodeString("UTF-8"))

    val dataSource = fileSource
      .collect[MessageType] {
        case ExtractMessage(zdt, server, message) => (zdt, server, message)
      }
      .dropWhile(_._1.isBefore(ZonedDateTime.parse(from)))
      .takeWhile(_._1.isBefore(ZonedDateTime.parse(to)))
      .map { case (a, b, c) => (a, b, LogController.filterOutIp(c)) }
      .filterNot {
        case (a, b, c) => LogController.ignorePrivateConversation(c)
      }
      .map {
        case (zdt, server, message) =>
          s"${DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant)}\t${server}\t${message}\n"
      }
    Ok.chunked(dataSource).as(LogController.TsvMimeType)
  }

}

object LogController {

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

}
