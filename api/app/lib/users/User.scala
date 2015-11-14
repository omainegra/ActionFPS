package lib.users

import java.time.ZonedDateTime

import play.api.libs.json._

case class CurrentNickname(nickname: String, countryCode: Option[String], from: ZonedDateTime)

case class PreviousNickname(nickname: String, countryCode: Option[String], from: ZonedDateTime, to: ZonedDateTime)

case class User(id: String, name: String, countryCode: Option[String], email: String,
                registrationDate: ZonedDateTime, nickname: CurrentNickname, previousNicknames: Option[List[PreviousNickname]])
object User {
  implicit val pnFormat = Json.format[PreviousNickname]
  implicit val cnFormat = Json.format[CurrentNickname]
  implicit val userFormat = Json.format[User]
  object WithoutEmailFormat {
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    implicit val noEmailUserWrite = Json.writes[User].transform(jv => jv.validate((__ \ 'email).json.prune).get)
  }
}