package controllers

/**
  * Created by me on 09/05/2016.
  */
import java.nio.file.{Path, Paths}
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser
import com.actionfps.ladder.parser._
import lib.WebTemplateRender
import play.api.Configuration
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.TsvLadderService
import views.ladder.Table.PlayerNamer

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class LadderController(journalPath: Path,
                       configuration: Configuration,
                       providesUsers: ProvidesUsers,
                       common: WebTemplateRender,
                       components: ControllerComponents)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem)
    extends AbstractController(components) {
  private implicit val actorMaterializer: ActorMaterializer =
    ActorMaterializer()

  private def nickToUser: Future[NickToUser] =
    providesUsers.users.map(users =>
      NickToUser { nickname =>
        users.find(_.hasNickname(nickname)).map(_.id)
    })

  private val ladderService =
    new TsvLadderService(journalPath.toAbsolutePath, () => nickToUser)

  ladderService.run()

  def aggregate: Future[Aggregate] = {
    async {
      val aggregate = await(ladderService.aggregate)
      aggregate.latestInstant match {
        case Some(latestInstant) =>
          aggregate.displayed(latestInstant).trimmed(latestInstant)
        case _ => aggregate
      }
    }
  }

  def ladder: Action[AnyContent] = Action.async { implicit req =>
    async {
      req.getQueryString("format") match {
        case Some("json") =>
          implicit val aggWriter: OWrites[Aggregate] = {
            implicit val usWriter: OWrites[UserStatistics] =
              Json.writes[UserStatistics]
            Json.writes[Aggregate]
          }
          Ok(Json.toJson(await(aggregate)))
        case _ =>
          implicit val playerNamer: PlayerNamer = PlayerNamer.fromMap(
            await(providesUsers.users).map(u => u.id -> u.name).toMap)
          Ok(
            common.renderTemplate(title = Some("Ladder"),
                                  jsonLink = Some("?format=json"))(
              views.html.ladder
                .big_table(
                  views.ladder.Table.render(
                    WebTemplateRender.wwwLocation.resolve("ladder_table.html"),
                    await(aggregate))(showTime = true))
            ))
      }
    }
  }
}
