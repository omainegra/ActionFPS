package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.JsonGamePlayer

/**
  * Created by William on 11/11/2015.
  */
@SerialVersionUID(1000L)
object FragMaster extends Incremental {

  override def levels = List(500, 1000, 2000, 5000, 10000)

  override type InputType = JsonGamePlayer

  override def filter(inputType: JsonGamePlayer): Option[Int] = {
    Option(inputType.frags)
  }

  override def levelDescription(level: Int): String = Map(
    500 -> "Well, that's a start.",
    1000 -> "Already lost count.",
    2000 -> "I'm quite good at this!",
    5000 -> "I've seen blood.",
    10000 -> "That Rambo guy got nothin' on me."
  ).getOrElse(level, s"Achieve $level frags")

  override def eventLevelTitle(level: Int): String = s"achieved Frag Master level $level"

  override def title: String = "Frag Master"

  override def levelTitle(level: Int): String = s"$title: $level"

}
