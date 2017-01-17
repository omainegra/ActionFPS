package com.actionfps.inter

/**
  * Created by me on 17/01/2017.
  */
case class InterMessage(ip: String, nickname: String)

object InterMessage {

  private val regex = """^\[([^\]]+)\] ([^ ]+) says: '(.+)'$""".r

  def unapply(input: String): Option[InterMessage] = {
    PartialFunction.condOpt(input) {
      case regex(ip, nickname, "!inter") => InterMessage(ip, nickname)
    }
  }
}
