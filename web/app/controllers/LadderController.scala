package controllers

/**
  * Created by me on 09/05/2016.
  */

import java.time.ZoneId
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

  import concurrent.duration._

  private def up = referenceProvider.syncUserProvider(10.seconds)

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
      .collect {
        case TimesMessage(TimesMessage(ldt, PlayerMessage(m))) =>
          agg.send(_.includeLine(s"${command}")(m.timed(ldt.atZone(ZoneId.of("UTC"))))(up))
      }
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
  def getSourceCommands(configuration: Configuration, path: String): Option[List[List[String]]] = {
    import collection.JavaConverters._
    configuration.getConfigList(path).map { items =>
      items.asScala.map { source =>
        source.underlying.getStringList("command").asScala.toList
      }.toList
    }
  }
}
