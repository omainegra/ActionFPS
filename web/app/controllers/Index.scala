package controllers

import javax.inject.{Inject, Singleton}

import org.apache.http.client.fluent.Request
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services.{AchievementsService, GamesService}

import scala.concurrent.ExecutionContext

/**
  * Created by William on 27/12/2015.
  */
@Singleton
class Index @Inject()(gamesService: GamesService,
                      achievementsService: AchievementsService, wSClient: WSClient)
                     (implicit executionContext: ExecutionContext) extends Controller {

  def index = Action {
    val events = Json.toJson(achievementsService.achievements.get().events.take(7))
    val recent = Json.toJson(gamesService.allGames.get().takeRight(50).map(_.toJson).reverse)

    //    val clanwar = Json.fromJson[Map[String, JsValue]](Json.parse(Request.Get("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1").execute().returnContent().asString()))
    //      .map(_.headOption.get._2).get
    val clanwar = JsNull

    val json = Json.toJson(Map("events" -> events, "recent" -> recent, "clanwar" -> clanwar))
    Ok(jsonToHtml("/", json))
//    wSClient.url("http://127.0.0.1:8888/").post(json).map { r =>
//      Ok(Html(r.body.replaceAllLiterally("""<script id="ga">""", """<script id="ga" type="ignore">""")))
//    }
  }

}
