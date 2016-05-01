package com.actionfps.accumulation

/**
  * Created by William on 26/12/2015.
  */

import java.time.{ZoneId, ZonedDateTime}

import com.actionfps.gameparser.enrichers.ViewFields
import com.actionfps.reference.{NicknameRecord, Registration}
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

case class User(id: String, name: String, countryCode: Option[String], email: Option[String],
                registrationDate: ZonedDateTime, nickname: CurrentNickname, previousNicknames: Option[List[PreviousNickname]]) {
  def nicknames: List[Nickname] = List(nickname) ++ previousNicknames.toList.flatten

  def validAt(nickname: String, zonedDateTime: ZonedDateTime) = nicknames.exists(n => n.nickname == nickname && n.validAt(zonedDateTime))
}

object User {
  implicit val vf = ViewFields.DefaultZonedDateTimeWrites
  implicit val pnFormat = Json.format[PreviousNickname]
  implicit val cnFormat = Json.format[CurrentNickname]
  implicit val userFormat = Json.format[User]

  object WithoutEmailFormat {

    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._

    implicit val noEmailUserWrite = Json.writes[User].transform((jv: JsObject) => jv.validate((__ \ 'email).json.prune).get)
  }

  def fromRegistration(registration: Registration, nicknames: List[NicknameRecord]): Option[User] = {
    val hisNicks = nicknames.filter(_.id == registration.id).sortBy(_.from.toString)
    PartialFunction.condOpt(hisNicks) {
      case nicks if nicks.nonEmpty =>
        val currentNickname = hisNicks.last
        val previousNicknames = hisNicks.sliding(2).collect {
          case List(nick, nextNick) =>
            PreviousNickname(
              nickname = nick.nickname,
              from = nick.from.atZone(ZoneId.of("UTC")),
              to = nextNick.from.atZone(ZoneId.of("UTC")),
              countryCode = None
            )
        }.toList
        User(
          id = registration.id,
          name = registration.name,
          countryCode = None,
          email = registration.email,
          registrationDate = registration.registrationDate.atZone(ZoneId.of("UTC")),
          nickname = CurrentNickname(
            nickname = currentNickname.nickname,
            countryCode = None,
            from = currentNickname.from.atZone(ZoneId.of("UTC"))
          ),
          previousNicknames = Option(previousNicknames).filter(_.nonEmpty)
        )
    }
  }
}