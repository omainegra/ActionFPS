package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 11/05/2017.
  */
case class TsvExtract(servers: Set[String], nickToUser: NickToUser) {
  private val regex = s"""\\[([^\\]]+)\\] ([^ ]+) (.*)""".r
  private val serversList = servers.toList
  def unapply(line: String): Option[(String, TimedUserMessage)] = {
    if (servers.exists(server => line.contains(server))) {
      val splittedLine = line.split('\t')
      if (splittedLine.length >= 3) {
        val server = splittedLine(1)
        if (servers.contains(server)) {
          val message = splittedLine(2)
          if (message.length > 0 && message.charAt(0) == '[') {
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
      } else None
    } else None
  }

  def unapplyHint(line: String,
                  payloadStart: Int,
                  nickname: String,
                  instantEnd: Int,
                  serverEnd: Int): Option[(String, TimedUserMessage)] = {
    val server = line.substring(instantEnd + 1, serverEnd)
    if (servers.contains(server)) {
      val message = line.substring(serverEnd + 1)
      if (message.length > 0 && message.charAt(0) == '[') {
        var offset = 0
        while (message.charAt(offset) != ' ') {
          offset = offset + 1
        }
        offset = offset + 1
        val nickStartPosition = offset
        while (message.charAt(offset) != ' ') {
          offset = offset + 1
        }
        val nickEndPosition = offset
        offset = offset + 1
        val payloadStartPosition = offset
        if (!nickToUser.nicknameExists(nickname))
          None
        else {
          val timestamp = Instant.parse(line.substring(0, instantEnd))
          nickToUser.userOfNickname(nickname, timestamp).map { user =>
            server -> TimedUserMessage(timestamp,
                                       user,
                                       message.substring(payloadStartPosition))
          }
        }
      } else None
    } else None
  }
}

object TsvExtract {

  def buildAggregate(source: scala.io.Source,
                     nickToUser: NickToUser): Aggregate = {

    val tsvExtract =
      TsvExtract(com.actionfps.ladder.parser.validServers, nickToUser)
    source
      .getLines()
      .foldLeft(Aggregate.empty) {
        case (ka, tsvExtract(_, tum)) =>
          ka.includeLine(tum)
        case (ka, _) => ka
      }
  }

  def empty =
    TsvExtract(servers = Set.empty, nickToUser = NickToUser(Map.empty))
}
