package com.actionfps

import java.time.ZonedDateTime

import scala.util.Try

/**
  * Created by William on 09/12/2015.
  */
package object inter {

  case class InterCall(time: ZonedDateTime, server: String, ip: String, nickname: String)

  object ZDT {
    def unapply(input: String): Option[ZonedDateTime] = {
      Try(ZonedDateTime.parse(input)).toOption
    }
  }

  object IntValue {
    def unapply(input: String): Option[Int] =
      Try(input.toInt).toOption
  }

  case class InterMessage(ip: String, nickname: String) {
    def toCall(time: ZonedDateTime, server: String) = InterCall(
      time = time,
      server = server,
      ip = ip,
      nickname = nickname
    )
  }

  object InterMessage {
    val matcher = s"""\\[([^ ]+)\\] ([^ ]+) says: '(.*)'""".r

    def unapply(input: String): Option[InterMessage] = PartialFunction.condOpt(input) {
      case matcher(ip, nickname, "!inter") =>
        InterMessage(ip, nickname)
    }
  }

}
