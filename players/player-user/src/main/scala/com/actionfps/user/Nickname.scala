package com.actionfps.user

import java.time.ZonedDateTime

import com.actionfps.user.Nickname.{CurrentNickname, PreviousNickname}

/**
  * Created by me on 15/01/2017.
  */
object Nickname {

  case class CurrentNickname(nickname: String, from: ZonedDateTime) extends Nickname

  case class PreviousNickname(nickname: String, from: ZonedDateTime, to: ZonedDateTime) extends Nickname

}

sealed trait Nickname {
  def nickname: String

  def from: ZonedDateTime

  def validAt(zonedDateTime: ZonedDateTime): Boolean = this match {
    case _: CurrentNickname => zonedDateTime.isAfter(from)
    case p: PreviousNickname => zonedDateTime.isAfter(from) && zonedDateTime.isBefore(p.to)
  }
}
