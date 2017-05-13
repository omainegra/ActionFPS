package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 11/05/2017.
  */
trait NickToUser {
  def nicknameExists(nickname: String): Boolean
  def userOfNickname(nickname: String, atTime: Instant): Option[String]
}

object NickToUser {
  def apply(f: String => Option[String]): NickToUser = new NickToUser {
    override def userOfNickname(nickname: String,
                                atTime: Instant): Option[String] =
      f(nickname)

    override def nicknameExists(nickname: String): Boolean =
      f(nickname).isDefined
  }
  def empty: NickToUser = apply(Function.const(None))
}
