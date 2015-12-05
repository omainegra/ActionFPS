package lib.users

import java.time.ZonedDateTime

import play.api.libs.json._

sealed trait Nickname {
  def nickname: String
  def countryCode: Option[String]
  def from: ZonedDateTime
  def validAt(zonedDateTime: ZonedDateTime): Boolean = this match {
    case c: CurrentNickname => zonedDateTime.isAfter(from)
    case p: PreviousNickname => zonedDateTime.isAfter(from) && zonedDateTime.isBefore(p.to)
  }
}

case class CurrentNickname(nickname: String, countryCode: Option[String], from: ZonedDateTime) extends Nickname

case class PreviousNickname(nickname: String, countryCode: Option[String], from: ZonedDateTime, to: ZonedDateTime) extends Nickname

case class User(id: String, name: String, countryCode: Option[String], email: String,
                registrationDate: ZonedDateTime, nickname: CurrentNickname, previousNicknames: Option[List[PreviousNickname]]) {
  def nicknames: List[Nickname] = List(nickname) ++ previousNicknames.toList.flatten
  def validAt(nickname: String, zonedDateTime: ZonedDateTime) = nicknames.exists(n => n.nickname == nickname && n.validAt(zonedDateTime))
}
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