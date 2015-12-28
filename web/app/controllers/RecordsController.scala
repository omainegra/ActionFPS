package controllers

import af.{User, Clan}
import af.rr.ServerRecord
import play.api.libs.json.Json
import javax.inject._

import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext

import scala.async.Async._

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class RecordsController @Inject()(recordsService: RecordsService, phpRenderService: PhpRenderService)
                                 (implicit executionContext: ExecutionContext) extends Controller {

  implicit val fmtClan = Json.format[Clan]

  implicit val serversWrites = Json.writes[ServerRecord]

  def getServers = Action.async { implicit req =>
    async {
      Ok(await(phpRenderService("/servers/", Json.toJson(recordsService.servers))))
    }
  }

}
