package controllers

/**
  * Created by me on 09/05/2016.
  */

import javax.inject._

import akka.agent.Agent
import com.actionfps.ladder.SshTailer
import com.actionfps.ladder.connecting.RemoteSshPath
import com.actionfps.ladder.parser.{Aggregate, LineParser, PlayerMessage}
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, Controller}
import play.api.{Configuration, Logger}
import providers.ReferenceProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LadderController @Inject
()(applicationLifecycle: ApplicationLifecycle,
   common: Common,
   configuration: Configuration,
   referenceProvider: ReferenceProvider)
(implicit executionContext: ExecutionContext) extends Controller {

  import collection.JavaConverters._

  val prs = LineParser(atYear = 2016)
  val agg = Agent(Aggregate.empty)

  import concurrent.duration._

  def up = referenceProvider.syncUserProvider(10.seconds)

  def includeLine(input: String): Unit = input match {
    case prs(_, PlayerMessage(pm)) =>
      agg.send(_.includeLine(pm)(up))
    case _ =>
  }

  val tailers = configuration.getStringList("af.ladder.sources").get.asScala.map { sshUrl =>
    Logger.info(s"Logging from ${sshUrl}")
    val q = new SshTailer(
      endOnly = false,
      file = RemoteSshPath.unapply(sshUrl).get
    )(includeLine)
    q
  }

  applicationLifecycle.addStopHook(() => Future.successful(tailers.foreach(_.shutdown())))

  def ladder = Action { implicit req =>
    Ok(common.renderTemplate(
      title = Some("Ladder"),
      supportsJson = false,
      login = None)
    (views.html.ladder.content(agg.get())))
  }
}
