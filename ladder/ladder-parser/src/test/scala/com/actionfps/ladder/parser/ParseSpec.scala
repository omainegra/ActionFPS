package com.actionfps.ladder.parser

import java.time.Instant

import org.scalatest._
import Matchers._
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

class ParseSpec extends FreeSpec {
  "it works" in {
    val inputMessage =
      "2017-01-07T23:55:05 [79.91.76.62] DaylixX gibbed w00p|Lucas"
    val tmu =
      TimedUserMessageExtract(NickToUser(Map("DaylixX" -> "daylixx").get))
        .unapply(inputMessage)

    val r = Aggregate.empty.includeLine(tmu.get)
    val user = r.users("daylixx")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }

  "it includes new data format" in {
    val inputMessage =
      "2014-12-13T18:36:16Z\twoop.ac:1999\t[0.0.0.0] .LeXuS'' headshot [PSY]quico"

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

    val tsvExtract = TsvExtract(servers = Set("woop.ac:1999"),
                                nickToUser =
                                  NickToUser(Map(".LeXuS''" -> "lexus").get))

    val tsvExtract(server, tum) = inputMessage

    val keyedAggregate = KeyedAggregate
      .empty[String]
      .includeLine(server)(tum)

    val user = keyedAggregate.total.users("lexus")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }
}
