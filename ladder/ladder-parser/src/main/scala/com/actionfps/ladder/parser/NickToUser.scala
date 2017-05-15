package com.actionfps.ladder.parser

/**
  * Created by me on 11/05/2017.
  */
case class NickToUser(nickToUser: Map[String, String])

object NickToUser {
  def empty: NickToUser = NickToUser(Map.empty)
}
