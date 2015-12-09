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

  object InterCall {
    val matcher = """Date: ([^ ]+), Server: [^ ]+ aura AssaultCube\[local#(\d+)\], Payload: \[([^ ]+)\] ([^ ]+) says: '(.*)'""".r

    def unapply(input: String): Option[InterCall] = {
      PartialFunction.condOpt(input) {
        case matcher(ZDT(time), IntValue(port), ip, nickname, "!inter") =>
          InterCall(
            time = time,
            server = s"aura.woop.ac:$port",
            ip = ip,
            nickname = nickname
          )
      }
    }
  }

}
