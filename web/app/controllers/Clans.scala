package controllers

import javax.inject._

import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext

import scala.async.Async._

/**
  * This API depends on the games
  */
@Singleton
class Clans @Inject()(recordsService: RecordsService,
                      wSClient: WSClient,
                      phpRenderService: PhpRenderService)
                     (implicit executionContext: ExecutionContext) extends Controller {

  def rankings = Action.async { implicit req =>
    async {
      val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10"
      val json = await(wSClient.url(url).get()).json
      await(phpRenderService(path = "/rankings", json = json))
    }
  }

  def clanwars = Action.async { implicit req =>
    async {
      val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50"
      val json = await(wSClient.url(url).get()).json
      await(phpRenderService(path = "/clanwars/", json))
    }
  }

  def clans = Action.async { implicit req =>
    phpRenderService(path = "/clans/", Json.toJson(recordsService.clans.map(_.toJson)))
  }

  def clan(id: String) = Action.async { implicit req =>
    async {
      val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php"
      val json = await(wSClient.url(url).withQueryString("id" -> id).execute()).json
      await(phpRenderService(path = "/clan/", json))
    }
  }

  def clanwar(id: String) = Action.async { implicit req =>
    async {
      val url = "http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwar.php"
      val json = await(wSClient.url(url).withQueryString("id" -> id).get()).json
      await(phpRenderService(path = "/clanwar/", json))
    }
  }

}
