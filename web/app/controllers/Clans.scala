package controllers

import javax.inject._

import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext


/**
  * This API depends on the games
  */
@Singleton
class Clans @Inject()(recordsService: RecordsService, wSClient: WSClient)
                     (implicit executionContext: ExecutionContext) extends Controller {

  def rankings = Action.async {
    val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10"
    wSClient.url(url).get().map(resp => Ok(jsonToHtml("/rankings/",resp.json)))
  }

  def clanwars = Action.async {
    val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50"
    wSClient.url(url).get().map(resp => Ok(jsonToHtml("/clanwars/", resp.json)))
  }

  def clans = Action {
    Ok(jsonToHtml("/clans/", Json.toJson(recordsService.clans.map(_.toJson))))
  }

  def clan(id: String) = Action.async {
    val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php"
    wSClient.url(url).withQueryString("id" -> id).execute()
    .map(resp => Ok(jsonToHtml("/clan/", resp.json)))
  }

  def clanwar(id: String) = Action.async {
    val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwar.php"
    wSClient.url(url).withQueryString("id" -> id).get().map{ response =>
      Ok(jsonToHtml("/clanwar/", response.json))
//      Ok(response.json)
    }
  }

}
