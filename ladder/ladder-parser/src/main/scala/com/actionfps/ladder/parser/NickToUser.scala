package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 11/05/2017.
  */
trait NickToUser {
  def nicknames: Set[String]
  def nickToUser: Map[String, String]
  def userOfNickname(nickname: String, atTime: Instant): Option[String]
  def nicknameExists(nickname: String): Boolean = nicknames.contains(nickname)
}

object NickToUser {
  def apply(n: Map[String, String]): NickToUser = {
    val nicksSet = n.keySet
    new NickToUser {
      override def userOfNickname(nickname: String,
                                  atTime: Instant): Option[String] =
        n.get(nickname)

      override def nicknames: Set[String] = nicksSet

      override def nickToUser: Map[String, String] = n
    }
  }
  def empty: NickToUser = apply(Map.empty)
}
