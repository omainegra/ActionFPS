package controllers

/**
  * Created by me on 09/05/2016.
  */

import java.time.ZoneId
import javax.inject._

import akka.agent.Agent
import com.actionfps.ladder.ProcessTailer
import com.actionfps.ladder.parser.ScannedTiming.After
import com.actionfps.ladder.parser._
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class LadderController @Inject
()(applicationLifecycle: ApplicationLifecycle,
   common: Common,
   configuration: Configuration,
   referenceProvider: ReferenceProvider)
(implicit executionContext: ExecutionContext) extends Controller {

  val agg = Agent(Aggregate.empty)

  import concurrent.duration._

  private def up = referenceProvider.syncUserProvider(10.seconds)

  private val tailers = LadderController
    .getSourceCommands(configuration, "af.ladder.sources")
    .toList
    .flatten.map { command =>
    try {
      Logger.info(s"Starting process = ${command}")
      var currentState = LineTimerScanner.empty
      val t = new ProcessTailer(command)({
        case DirectTimedLine(dtl) =>
          currentState = currentState.include(dtl)
          currentState.emitLine.foreach {
            case ScanTimedLine(After(tm), PlayerMessage(m)) =>
              agg.send(_.includeLine(m.timed(tm.atZone(ZoneId.of("UTC"))))(up))
            case _ =>
          }
        case _ =>
      })
      t
    } catch {
      case NonFatal(e) =>
        Logger.error(s"Failed to start: ${command}", e)
        throw e
    }
  }

  applicationLifecycle.addStopHook(() => Future.successful(tailers.foreach(_.shutdown())))

  def ladder = Action { implicit req =>
    req.getQueryString("format") match {
      case Some("json") =>
        implicit val aggWriter = {
          implicit val usWriter = Json.writes[UserStatistics]
          Json.writes[Aggregate]
        }
        Ok(Json.toJson(agg.get()))
      case _ =>
        Ok(common.renderTemplate(
          title = Some("Ladder"),
          supportsJson = true,
          login = None)
        (views.ladder.Table.render(agg.get())(showTime = true)))
    }
  }
}

object LadderController {
  def getSourceCommands(configuration: Configuration, path: String): Option[List[List[String]]] = {
    import collection.JavaConverters._
    configuration.getConfigList(path).map { items => items.asScala.map { source =>
      source.underlying.getStringList("command").asScala.toList
    }.toList
    }
  }
}
