package com.actionfps.accumulation.user

/**
  * Created by William on 26/12/2015.
  */

import java.time.{ZoneId, ZonedDateTime}

import com.actionfps.accumulation.user.Nickname.{CurrentNickname, PreviousNickname}
import com.actionfps.reference.Registration.Email
import com.actionfps.reference.{NicknameRecord, Registration}

case class User(id: String, name: String, email: Email,
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
        User(
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
