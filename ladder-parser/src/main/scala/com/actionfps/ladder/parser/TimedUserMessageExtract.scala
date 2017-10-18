package com.actionfps.ladder.parser

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

/**
  * Created by me on 11/05/2017.
  */
case class TimedUserMessageExtract(nickToUser: NickToUser) {
  def unapply(input: String): Option[TimedUserMessage] = {
    val localTimeSample = "2016-07-02T22:09:14"
    val regex = s"""\\[([^\\]]+)\\] ([^ ]+) (.*)""".r
    val firstSpace = input.indexOf(' ')
    if (firstSpace < 10) None
    else {
      val (time, rest) = input.splitAt(firstSpace)
      val msg = rest.drop(1)
      val instant = {
        if (time.length == localTimeSample.length)
          LocalDateTime.parse(time).toInstant(ZoneOffset.UTC)
        else
          ZonedDateTime.parse(time).toInstant
      }
      msg match {
        case regex(ip, nickname, mesg) =>
          nickToUser.userOfNickname(nickname) match {
            case Some(user) =>
              Some(TimedUserMessage(instant, user, mesg))
            case _ => None
          }
        case _ => None
      }
    }
  }
}

object TimedUserMessageExtract {

  trait NickToUser {
    def userOfNickname(nickname: String): Option[String]
  }

  object NickToUser {
    def apply(f: String => Option[String]): NickToUser = (nickname: String) => f(nickname)
    def empty: NickToUser = Function.const(None)
  }
}
