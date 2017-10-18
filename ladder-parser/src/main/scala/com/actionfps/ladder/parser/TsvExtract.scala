package com.actionfps.ladder.parser

import java.time.Instant

import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

/**
  * Created by me on 11/05/2017.
  */
case class TsvExtract(servers: Set[String], nickToUser: NickToUser) {
  def unapply(line: String): Option[(String, TimedUserMessage)] = {
    val splittedLine = line.split("\t", -1)
    val server = splittedLine(1)
    if (splittedLine.length >= 3 && servers.contains(server)) {
      val message = splittedLine(2)
      val regex = s"""\\[([^\\]]+)\\] ([^ ]+) (.*)""".r
      message match {
        case regex(ip, nick, content) =>
          nickToUser.userOfNickname(nick) match {
            case Some(uuser) =>
              val timestamp = Instant.parse(splittedLine(0))
              Some(server -> TimedUserMessage(timestamp, uuser, content))
            case None => None
          }
        case _ => None
      }
    } else None
  }
}
object TsvExtract {
  def empty =
    TsvExtract(servers = Set.empty,
               nickToUser = NickToUser(Function.const(None)))
}
