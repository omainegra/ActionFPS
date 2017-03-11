package controllers

/**
  * Created by me on 09/05/2016.
  */

import java.time.Instant
import javax.inject._

import akka.actor.ActorSystem
import com.actionfps.ladder.parser._
import controllers.LadderController.PlayerNamer
import lib.WebTemplateRender
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, Controller, ControllerComponents}
import providers.ReferenceProvider
import services.LadderService

import scala.concurrent.ExecutionContext
import scala.async.Async._

@Singleton
class LadderController @Inject()(ladderService: LadderService,
                                 referenceProvider: ReferenceProvider,
                                 common: WebTemplateRender,
                                 components: ControllerComponents)
                                (implicit executionContext: ExecutionContext,
                                 actorSystem: ActorSystem) extends AbstractController(components) {

  def aggregate: Aggregate = ladderService.aggregate.displayed(Instant.now()).trimmed(Instant.now())

  def ladder = Action.async { implicit req =>
    async {
      req.getQueryString("format") match {
        case Some("json") =>
          implicit val aggWriter = {
            implicit val usWriter = Json.writes[UserStatistics]
            Json.writes[Aggregate]
          }
          Ok(Json.toJson(aggregate))
        case _ =>
          implicit val playerNamer = PlayerNamer.fromMap(await(referenceProvider.Users.users).map(u => u.id -> u.name).toMap)
          Ok(common.renderTemplate(
            title = Some("Ladder"),
            supportsJson = true)
          (views.ladder.Table.render(aggregate)(showTime = true)))
      }
    }
  }
}

object LadderController {

  trait PlayerNamer {
    def nameOf(user: String): Option[String]
  }

  object PlayerNamer {
    def fromMap(map: Map[String, String]): PlayerNamer = new PlayerNamer {
      override def nameOf(user: String): Option[String] = map.get(user)
    }
  }

}
