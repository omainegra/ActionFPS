package com.actionfps.ladder.parser

/**
  * Created by me on 16/05/2017.
  */
sealed abstract class UserAction(val apply: UserStatistics => UserStatistics) {}

object UserAction {
  case object Killed extends UserAction(_.kill)
  case object Gibbed extends UserAction(_.gib)
  case object Scored extends UserAction(_.flag)
  val actionToWords: Map[UserAction, Set[String]] =
    Map(Killed -> killWords, Gibbed -> gibWords, Scored -> scoredWords)
  val wordToAction: Map[String, UserAction] = actionToWords.flatMap {
    case (a, ws) =>
      ws.map { w =>
        w -> a
      }
  }
}
