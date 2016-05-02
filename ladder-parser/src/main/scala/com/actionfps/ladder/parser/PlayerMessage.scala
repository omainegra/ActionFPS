package com.actionfps.ladder.parser

/**
  * Created by me on 02/05/2016.
  */
object PlayerMessage {
  def unapply(input: String): Option[PlayerMessage] = {
    val regex = s"""\\[([\\d\\.]+)\\] ([^ ]+) (.*)""".r
    PartialFunction.condOpt(input) {
      case regex(ip, name, message) => PlayerMessage(
        ip = ip,
        name = name,
        message = message
      )
    }
  }
}

case class PlayerMessage(ip: String, name: String, message: String) {

  def words = message.split(" ").toList

  def killed: Option[String] =
    PartialFunction.condOpt(words) {
      case killWord :: who :: Nil if killWords.contains(killWord) =>
        who
    }

  def gibbed: Option[String] =
    PartialFunction.condOpt(words) {
      case gibWord :: who :: Nil if gibWords.contains(gibWord) =>
        who
    }

  def scored: Boolean =
    words.headOption.contains("scored")
}