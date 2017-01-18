package controllers

/**
  * Created by me on 09/05/2016.
  */

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.actionfps.ladder.parser._
import lib.WebTemplateRender
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider

import scala.concurrent.ExecutionContext

@Singleton
class LadderController @Inject
()(applicationLifecycle: ApplicationLifecycle,
   common: WebTemplateRender,
   configuration: Configuration,
   referenceProvider: ReferenceProvider)
(implicit executionContext: ExecutionContext,
 actorSystem: ActorSystem) extends Controller {

  private val agg = Agent(KeyedAggregate.empty[String])

  implicit val actorMat = ActorMaterializer()

  def aggregate: Aggregate = agg.get().total

  private val tailers = LadderController
    .getSourceCommands(configuration, "af.ladder.sources")
    .toList
    .flatten.map { command =>
    StreamConverters.fromInputStream(() => {
      new java.lang.ProcessBuilder(command: _*)
        .start()
        .getInputStream
    })
      .via(Framing.delimiter(ByteString.fromString("\n", "UTF-8"), 2096, allowTruncation = false))
      .map(_.decodeString("UTF-8"))
      .scanAsync(Aggregate.empty) {
        case (aggregate, message) =>
          import scala.async.Async._
          async {
            val n2u = await(referenceProvider.users).map(u => u.nickname.nickname -> u.id).toMap
            val tmu = LadderController.TimedUserMessageExtract(n2u.get)
            message match {
              case tmu(timedUserMessage) => aggregate.includeLine(timedUserMessage)
              case _ => aggregate
            }
          }
      }
      .alsoTo(Sink.foreach { g => agg.send(_.includeAggregate(s"$command")(g)) })
      .to(Sink.ignore)
      .run()
  }

  def ladder = Action { implicit req =>
    req.getQueryString("format") match {
      case Some("json") =>
        implicit val aggWriter = {
          implicit val usWriter = Json.writes[UserStatistics]
          Json.writes[Aggregate]
        }
        Ok(Json.toJson(agg.get().total))
      case _ =>
        Ok(common.renderTemplate(
          title = Some("Ladder"),
          supportsJson = true)
        (views.ladder.Table.render(agg.get().total)(showTime = true)))
    }
  }
}

object LadderController {

  case class TimedUserMessageExtract(nickToUser: String => Option[String]) {
    def unapply(input: String): Option[TimedUserMessage] = {
      val localTimeSample = "2016-07-02T22:09:14"
      val regex = s"""\\[([\\d\\.]+)\\] ([^ ]+) (.*)""".r
      val firstSpace = input.indexOf(' ')
      if (firstSpace < 10) None else {
        val (time, rest) = input.splitAt(firstSpace)
        val msg = rest.drop(1)
        val instant = {
          if (time.length == localTimeSample.length)
            LocalDateTime.parse(time).toInstant(ZoneOffset.UTC)
          else
            ZonedDateTime.parse(time).toInstant
        }
        msg match {
          case regex(ip, nickname, mesg) =>
            nickToUser(nickname) match {
              case Some(user) =>
                Some(TimedUserMessage(instant, user, mesg))
              case _ => None
            }
          case _ => None
        }
      }
    }
  }

  def getSourceCommands(configuration: Configuration, path: String): Option[List[List[String]]] = {
    import collection.JavaConverters._
    configuration.getConfigList(path).map { items =>
      items.asScala.map { source =>
        source.underlying.getStringList("command").asScala.toList
      }.toList
    }
  }
}
