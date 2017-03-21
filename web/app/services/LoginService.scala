package services

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import javax.inject._

import com.actionfps.accumulation.user.User
import play.api.Configuration
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import rapture.json._
import rapture.json.jsonBackends.play._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 21/03/2017.
  */
@Singleton
class LoginService(spreadsheetId: String)(implicit executionContext: ExecutionContext, wsClient: WSClient) {

  @Inject() def this(configuration: Configuration)(implicit executionContext: ExecutionContext, wsClient: WSClient) =
    this(configuration.underlying.getString("spreadsheet-id"))

  val SheetNicknames = "Nicknames"
  val SheetRegistrations = "Registrations"

  def register_user(id: String, name: String, nickname: String, email: String, registrationDate: String): Option[List[String]] = {
    val errors = LoginService.verifyRegistration(???)(nickname, id, name, email)
    errors match {
      case None =>
        val url = s"https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}/values/Registrations:append"
        val postBody: JsObject = json"""{"values": [[
          ${id},${name},${email},${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.of("UTC")))}
          ]]}""".as[JsObject]
        val url2 = s"https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}/values/Nicknames:append"
        val postBody2: JsObject = json"""{"values": [[
          ${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.of("UTC")))},${id},${nickname}
          ]]}""".as[JsObject]
        wsClient.url(url).post(postBody)
        wsClient.url(url2).post(postBody2)
        None
      case other => other
    }
  }

  def getEmailFromToken(token: String): Future[String] = {
    wsClient.url("https://www.googleapis.com/oauth2/v3/tokeninfo")
      .withQueryString("id_token" -> token)
      .get().map { r =>
      (r.json \ "email").as[String]
    }
  }

}

object LoginService {
  def verifyRegistration(users: List[User])(newNickname: String, newUser: String, name: String, email: String): Option[List[String]] = {
    val errors = mutable.Buffer.empty[String]
    if ("/^[a-z]{3,}$/".r.findAllMatchIn(newUser).isEmpty) {
      errors += "User ID does not match expected format"
    }
    if ("/^[A-Z]?[a-z]{2,}$/".r.findAllMatchIn(name).isEmpty) {
      errors += "Username does not match expected format"
    }
    if ("/\\s/".r.findAllMatchIn(newNickname).isEmpty) {
      errors += "Nickname does not match expected format"
    }
    if (users.exists(_.nicknames.exists(_.nickname.equalsIgnoreCase(newNickname)))) {
      errors += s"Nickname ${newNickname} currently in use"
    }
    if (users.exists(_.name.equalsIgnoreCase(name))) {
      errors += s"Name ${name} already in use"
    }
    if (users.exists(_.id.equalsIgnoreCase(newUser))) {
      errors += s"User ID ${newUser} already in use"
    }
    if (users.exists(_.email.matches(email))) {
      errors += "e-mail '" + email + "' already in use"
    }
    Option(errors.toList).filter(_.nonEmpty)
  }
}
