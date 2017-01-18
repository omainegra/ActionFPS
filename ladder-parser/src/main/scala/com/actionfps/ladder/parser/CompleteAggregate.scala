package com.actionfps.ladder.parser

case class CompleteAggregate(all: Aggregate) {
  def includeLine(playerMessage: TimedUserMessage): CompleteAggregate = {
    copy(all = all.includeLine(playerMessage))
  }
}
