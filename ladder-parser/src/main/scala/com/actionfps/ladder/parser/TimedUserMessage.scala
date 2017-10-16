package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 08/01/2017.
  */
case class TimedUserMessage(instant: Instant, user: String, message: String) {
  private def words: List[String] = message.split(" ").toList

  def killed: Boolean = words.headOption.exists(killWords.contains)

  def gibbed: Boolean = words.headOption.exists(gibWords.contains)

  def scored: Boolean = words.headOption.contains("scored")
}
