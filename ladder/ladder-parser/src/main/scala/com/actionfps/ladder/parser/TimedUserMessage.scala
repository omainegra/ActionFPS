package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 08/01/2017.
  */
case class TimedUserMessage(instant: Instant, user: String, message: String) {
  private def headWord: Option[String] = {
    val idx = message.indexOf(" ")
    if (idx >= 0) Some(message.substring(0, idx)) else None
  }

  def killed: Boolean = headWord.exists(killWords.contains)

  def gibbed: Boolean = headWord.exists(gibWords.contains)

  def scored: Boolean = headWord.exists(scoredWords.contains)
}
