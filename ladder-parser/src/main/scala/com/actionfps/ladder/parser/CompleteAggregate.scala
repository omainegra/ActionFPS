package com.actionfps.ladder.parser

case class CompleteAggregate(all: Aggregate) {
  def includeLine(playerMessage: TimedPlayerMessage)(implicit userProvider: UserProvider): CompleteAggregate = {
    copy(all = all.includeLine(playerMessage))
  }
}








