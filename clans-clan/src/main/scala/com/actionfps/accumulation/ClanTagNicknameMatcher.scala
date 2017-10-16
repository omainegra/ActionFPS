package com.actionfps.accumulation

/**
  * Created by William on 26/12/2015.
  * {{{
  *   scala> import com.actionfps.accumulation.user._
  *   import com.actionfps.accumulation.user._
  *
  *   scala> ClanTagNicknameMatcher("w00p|*")("w00p|Drakas")
  *   res0: Boolean = true
  *
  *   scala> ClanTagNicknameMatcher("w00p|*")("w00ps|Drakas")
  *   res1: Boolean = false
  *
  *   scala> ClanTagNicknameMatcher("*|w00p")("w00ps|Drakas")
  *   res2: Boolean = false
  *
  *   scala> ClanTagNicknameMatcher("*|w00p")("Drakas|w00p")
  *   res3: Boolean = true
  *
  * }}}
  */
object ClanTagNicknameMatcher {

  /**
    * Match clan tag format with a nickname
    * @param format eg w00p|* or *|w00p
    * @param nickname the nickname
    * @return whether a match is fine.
    */
  def apply(format: String)(nickname: String): Boolean = {
    if (format.endsWith("*") && format.startsWith("*")) {
      nickname.contains(format.drop(1).dropRight(1))
    } else if (format.endsWith("*")) {
      nickname.startsWith(format.dropRight(1))
    } else if (format.startsWith("*")) {
      nickname.endsWith(format.drop(1))
    } else {
      false
    }
  }
}
