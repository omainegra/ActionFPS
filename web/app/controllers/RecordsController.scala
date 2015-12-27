package controllers

import af.{User, Clan}
import af.rr.ServerRecord
import play.api.libs.json.Json
import javax.inject._

import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class RecordsController @Inject()(recordsService: RecordsService)
                                 (implicit executionContext: ExecutionContext) extends Controller {

  implicit val fmtClan = Json.format[Clan]

  implicit val serversWrites = Json.writes[ServerRecord]

  def getServers = Action {
    Ok(jsonToHtml("/servers/", Json.toJson(recordsService.servers)))
  }

  def usersJson = Action {
    import User.WithoutEmailFormat.noEmailUserWrite
    Ok(Json.toJson(recordsService.users))
  }

  def userJson(id: String) = Action {
    recordsService.users.find(user => user.id == id || user.email == id) match {
      case Some(user) =>
        import User.WithoutEmailFormat.noEmailUserWrite
        Ok(Json.toJson(user))
      case None =>
        NotFound("User not found")
    }
  }

  def clans = Action {
    Ok(Json.toJson(recordsService.clans))
  }
}
