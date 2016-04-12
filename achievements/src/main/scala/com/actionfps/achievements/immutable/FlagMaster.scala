package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.{JsonGame, JsonGamePlayer}

/**
  * Created by William on 11/11/2015.
  */
@SerialVersionUID(1002L)
object FlagMaster extends Incremental {

  override def levels = List(50, 100, 200, 500, 1000)

  override type InputType = (JsonGame, JsonGamePlayer)

  override def filter(inputType: (JsonGame, JsonGamePlayer)): Option[Int] = {
    inputType match {
      case (game, player) if game.mode == "ctf" => player.flags
      case _ => None
    }
  }

  override def levelDescription(level: Int): String = Map(
    50 -> "What's that blue thing?",
    100 -> "I'm supposed to bring this back?",
    200 -> "What do you mean it's TDM?",
    500 -> "Yeah, I know where it goes.",
    1000 -> "Can I keep one at least?"
  ).getOrElse(level, s"Achieve $level flags")

  override def eventLevelTitle(level: Int): String = s"achieved Flag Master level $level"

  override def title: String = "Flag Master"

  override def levelTitle(level: Int): String = s"$title: $level"
}
