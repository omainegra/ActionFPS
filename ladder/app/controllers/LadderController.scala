package controllers

/**
  * Created by me on 09/05/2016.
  */
import java.time.Instant
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionfps.ladder.parser._
import lib.WebTemplateRender
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.twirl.api.Html
import services.LadderService
import services.LadderService.NickToUser
import views.ladder.Table.PlayerNamer

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LadderController @Inject()(configuration: Configuration,
                                 providesUsers: ProvidesUsers,
                                 common: WebTemplateRender)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem)
    extends Controller {
  private implicit val actorMaterializer = ActorMaterializer()

  private def nickToUser: Future[NickToUser] =
    providesUsers.users.map(users =>
      new NickToUser {
        override def userOfNickname(nickname: String): Option[String] = {
          users.find(_.nickname.nickname == nickname).map(_.id)
        }
    })

  private val ladderService = new LadderService(
    LadderService.getSourceCommands(configuration, "af.ladder.sources"),
    usersMap = {
      val f = nickToUser
      () =>
        f
    })

  ladderService.run()

  def aggregate: Aggregate =
    ladderService.aggregate.displayed(Instant.now()).trimmed(Instant.now())

  def ladder: Action[AnyContent] = Action.async { implicit req =>
    async {
      req.getQueryString("format") match {
        case Some("json") =>
          implicit val aggWriter = {
            implicit val usWriter = Json.writes[UserStatistics]
            Json.writes[Aggregate]
          }
          Ok(Json.toJson(aggregate))
        case _ =>
          implicit val playerNamer = PlayerNamer.fromMap(
            await(providesUsers.users).map(u => u.id -> u.name).toMap)
          Ok(
            common.renderTemplate(title = Some("Ladder"),
                                  jsonLink = Some("?format=json"))(
              views.html.ladder
                .big_table(
                  views.ladder.Table.render(
                    WebTemplateRender.wwwLocation.resolve("ladder_table.html"),
                    aggregate)(showTime = true))
            ))
      }
    }
  }
}
