package controllers

import javax.inject.{Inject, Singleton}

import org.apache.http.client.fluent.Request
import play.api.http.Writeable
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{RequestHeader, Action, Controller}
import play.twirl.api.Html
import services.{PhpRenderService, AchievementsService, GamesService}

import scala.concurrent.ExecutionContext

/**
  * Created by William on 27/12/2015.
  */
@Singleton
class Index @Inject()(gamesService: GamesService,
                      achievementsService: AchievementsService,
                      wSClient: WSClient,
                      phpRenderService: PhpRenderService)
                     (implicit executionContext: ExecutionContext) extends Controller {

  import scala.async.Async._

  def index = Action.async { implicit req =>
    async {
      val events = Json.toJson(achievementsService.achievements.get().events.take(7))
      val recent = Json.toJson(gamesService.allGames.get().takeRight(50).map(_.toJson).reverse)
      val csUrl = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1"
      val clanwar = Json.fromJson[Map[String, JsValue]](await(wSClient.url(csUrl).get()).json)
        .map(_.headOption.get._2).get
      val json = Json.toJson(Map("events" -> events, "recent" -> recent, "clanwar" -> clanwar))
      Ok(await(phpRenderService("/", json)))
    }
  }

  def login = Action.async { implicit req =>
    phpRenderService("/login/", JsNull).map(resp => Ok(resp))
  }

  def questions = Action.async { implicit req =>
    phpRenderService("/questions/", JsNull).map(resp => Ok(resp))
  }

  def api = Action.async { implicit req =>
    phpRenderService("/api/", JsNull).map(resp => Ok(resp))
  }

  def client = Action.async { implicit req =>
    phpRenderService("/client/", JsNull).map(resp => Ok(resp))
  }

}
