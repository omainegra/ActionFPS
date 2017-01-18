package controllers

/**
  * Created by me on 09/05/2016.
  */

import javax.inject._

import akka.actor.ActorSystem
import com.actionfps.ladder.parser._
import lib.WebTemplateRender
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.LadderService

import scala.concurrent.ExecutionContext

@Singleton
class LadderController @Inject()(ladderService: LadderService,
                                 common: WebTemplateRender)
                                (implicit executionContext: ExecutionContext,
                                 actorSystem: ActorSystem) extends Controller {

  def aggregate: Aggregate = ladderService.aggregate


  def ladder = Action { implicit req =>
    req.getQueryString("format") match {
      case Some("json") =>
        implicit val aggWriter = {
          implicit val usWriter = Json.writes[UserStatistics]
          Json.writes[Aggregate]
        }
        Ok(Json.toJson(aggregate))
      case _ =>
        Ok(common.renderTemplate(
          title = Some("Ladder"),
          supportsJson = true)
        (views.ladder.Table.render(aggregate)(showTime = true)))
    }
  }
}
