package com.actionfps.user

/**
  * Created by William on 26/12/2015.
  */

import java.time.{ZoneId, ZonedDateTime}

import com.actionfps.reference.{NicknameRecord, Registration, RegistrationEmail}
import com.actionfps.user.Nickname.{CurrentNickname, PreviousNickname}

case class User(id: String, name: String, email: RegistrationEmail,
                registrationDate: ZonedDateTime, nickname: CurrentNickname,
                previousNicknames: Option[List[PreviousNickname]]) {
  def nicknames: List[Nickname] = List(nickname) ++ previousNicknames.toList.flatten

  def validAt(nickname: String, zonedDateTime: ZonedDateTime): Boolean = {
    nicknames.exists(n => n.nickname == nickname && n.validAt(zonedDateTime))
  }
}

object User {

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
              to = nextNick.from.atZone(ZoneId.of("UTC"))
            )
        }.toList
        com.actionfps.user.User(
          id = registration.id,
          name = registration.name,
          email = registration.email,
          registrationDate = registration.registrationDate.atZone(ZoneId.of("UTC")),
          nickname = CurrentNickname(
            nickname = currentNickname.nickname,
            from = currentNickname.from.atZone(ZoneId.of("UTC"))
          ),
          previousNicknames = Option(previousNicknames).filter(_.nonEmpty)
        )
    }
  }
}
