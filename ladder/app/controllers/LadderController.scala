package controllers

/**
  * Created by me on 09/05/2016.
  */
import java.nio.file.Paths
import java.time.Instant
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionfps.ladder.parser._
import com.actionfps.user.User
import lib.WebTemplateRender
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import services.TsvLadderService
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

  private def nickToUser: Future[NickToUser] = async {
    LadderController.nickToUserFromUsers(await(providesUsers.users))
  }

  private val ladderService = new TsvLadderService(
    Paths
      .get(configuration.underlying.getString("journal.large"))
      .toAbsolutePath,
    () => nickToUser)

  ladderService.run()

  def aggregate: Future[Aggregate] =
    ladderService.aggregate.map(
      _.displayed(Instant.now()).trimmed(Instant.now()))

  def ladder: Action[AnyContent] = Action.async { implicit req =>
    async {
      req.getQueryString("format") match {
        case Some("json") =>
          implicit val aggWriter = {
            implicit val usWriter = Json.writes[UserStatistics]
            Json.writes[Aggregate]
          }
          Ok(Json.toJson(await(aggregate)))
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
                    await(aggregate))(showTime = true))
            ))
      }
    }
  }
}
object LadderController {
  def nickToUserFromUsers(users: List[User]): NickToUser = {
    val nickToUsers = users
      .flatMap { u =>
        u.nicknames.map(_.nickname).map { n =>
          n -> u
        }
      }
      .groupBy(_._1)
      .map {
        case (n, us) =>
          n -> us.map(_._2)
      }
    val nicks = nickToUsers.keySet
    new NickToUser {
      def nicknames: Set[String] = nicks

      override def userOfNickname(nickname: String,
                                  atTime: Instant): Option[String] = {
        nickToUsers
          .get(nickname)
          .flatMap { nickUsers =>
            // check if user is valid at this time
            // but alternatively, the ladder supports a mode
            // where we include all data before the player registered.
            // This is so that people receive points for the work they've
            // already done when they were not registered.
            nickUsers.headOption
//            def originalNicknameUser = nickUsers.find { user =>
//              user.nicknames
//                .sortBy(_.from)
//                .headOption
//                .exists(_.from.isBefore(atTime))
//            }
//            nickUsers.find(_.validAt(nickname, atTime)).orElse {
//              originalNicknameUser
//            }
          }
          .map(_.id)
      }

      override def nicknameExists(nickname: String): Boolean =
        nickToUsers.contains(nickname)
      private val n2u = nickToUsers.map { case (k, v) => k -> v.head.id }
      override def nickToUser: Map[String, String] = n2u
    }
  }
}
