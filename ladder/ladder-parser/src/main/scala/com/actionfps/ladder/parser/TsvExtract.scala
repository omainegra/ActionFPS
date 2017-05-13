package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 11/05/2017.
  */
case class TsvExtract(servers: Set[String], nickToUser: NickToUser) {
  def unapply(line: String): Option[(String, TimedUserMessage)] = {
    if (servers.exists(server => line.contains(server))) {
      val splittedLine = line.split("\t", -1)
      val server = splittedLine(1)
      if (splittedLine.length >= 3 && servers.contains(server)) {
        val message = splittedLine(2)
        val regex = s"""\\[([^\\]]+)\\] ([^ ]+) (.*)""".r
        message match {
          case regex(ip, nick, content) =>
            val timestamp = Instant.parse(splittedLine(0))
            nickToUser.userOfNickname(nick, timestamp) match {
              case Some(uuser) =>
                Some(server -> TimedUserMessage(timestamp, uuser, content))
              case None => None
            }
          case _ => None
        }
      } else None
    } else None
  }
}

object TsvExtract {
  def empty =
    TsvExtract(servers = Set.empty,
               nickToUser = NickToUser(Function.const(None)))
}
