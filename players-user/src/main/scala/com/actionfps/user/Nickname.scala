package com.actionfps.user

import java.time.{Instant, ZonedDateTime}

import com.actionfps.user.Nickname.{CurrentNickname, PreviousNickname}

/**
  * Created by me on 15/01/2017.
  */
object Nickname {

  case class CurrentNickname(nickname: String, from: Instant) extends Nickname

  case class PreviousNickname(nickname: String, from: Instant, to: Instant)
      extends Nickname

}

sealed trait Nickname {
  def nickname: String

  def from: Instant

  def validAt(instant: Instant): Boolean = this match {
    case _: CurrentNickname => instant.isAfter(from)
    case p: PreviousNickname => instant.isAfter(from) && instant.isBefore(p.to)
  }
}
