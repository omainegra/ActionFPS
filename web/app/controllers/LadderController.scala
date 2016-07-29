package controllers

/**
  * Created by me on 09/05/2016.
  */

import javax.inject._

import akka.agent.Agent
import com.actionfps.ladder.ProcessTailer
import com.actionfps.ladder.parser.{Aggregate, LineParser, PlayerMessage, UserStatistics}
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

  import collection.JavaConverters._


  val agg = Agent(Aggregate.empty)

  import concurrent.duration._

  def up = referenceProvider.syncUserProvider(10.seconds)

  def includeLine(prs: LineParser)(input: String): Unit = input match {
    case prs(time, PlayerMessage(pm)) =>
      agg.send(_.includeLine(pm.timed(time))(up))
    case _ =>
  }

  val tailers = configuration.getConfigSeq("af.ladder.sources").toSeq.flatten.map { source =>
    val command = source.underlying.getStringList("command").asScala.toList
    val year = source.underlying.getInt("year")
    val prs = LineParser(atYear = year)
    try {
      Logger.info(s"Starting process = ${command}")
      val t = new ProcessTailer(command)(line => includeLine(prs)(line))
      t
    } catch {
      case NonFatal(e) =>
        Logger.error(s"Failed to start: ${command}", e)
        throw e
    }
  }.toList

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
        (views.html.ladder.ladder_table(agg.get())(showTime = true)))
    }
  }
}
