package com.actionfps.user

/**
  * Created by William on 26/12/2015.
  */
import java.time.{Instant, ZoneId, ZonedDateTime}

import com.actionfps.user.Nickname.{CurrentNickname, PreviousNickname}

case class User(id: String,
                name: String,
                email: RegistrationEmail,
                registrationDate: ZonedDateTime,
                nickname: CurrentNickname,
                previousNicknames: Option[List[PreviousNickname]]) { u =>

  // not guaranteed to be ordered
  def nicknames: List[Nickname] = {
    nickname :: previousNicknames.getOrElse(Nil)
  }

  def hasNickname(nickname: String): Boolean =
    u.nickname.nickname == nickname || previousNicknames.exists(
      _.exists(_.nickname == nickname))

  // optimised
  def validAt(nickname: String, instant: Instant): Boolean = {
    def currentNicknameValid =
      u.nickname.nickname == nickname && u.nickname.validAt(instant)
    def anyOtherValid =
      previousNicknames.exists(
        _.exists(n => n.nickname == nickname && n.validAt(instant)))
    currentNicknameValid || anyOtherValid
  }
}

object User {

  def fromRegistration(registration: Registration,
                       nicknames: List[NicknameRecord]): Option[User] = {
    val hisNicks =
      nicknames.filter(_.id == registration.id).sortBy(_.from.toString)
    PartialFunction.condOpt(hisNicks) {
      case nicks if nicks.nonEmpty =>
        val currentNickname = hisNicks.last
        val previousNicknames = hisNicks
          .sliding(2)
          .collect {
            case List(nick, nextNick) =>
              PreviousNickname(
                nickname = nick.nickname,
                from = nick.from.atZone(ZoneId.of("UTC")).toInstant,
                to = nextNick.from.atZone(ZoneId.of("UTC")).toInstant
              )
          }
          .toList
        com.actionfps.user.User(
          id = registration.id,
          name = registration.name,
          email = registration.email,
          registrationDate =
            registration.registrationDate.atZone(ZoneId.of("UTC")),
          nickname = CurrentNickname(
            nickname = currentNickname.nickname,
            from = currentNickname.from.atZone(ZoneId.of("UTC")).toInstant
          ),
          previousNicknames = Option(previousNicknames).filter(_.nonEmpty)
        )
    }
  }
}
