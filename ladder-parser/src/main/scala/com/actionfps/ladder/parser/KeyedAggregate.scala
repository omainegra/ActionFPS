package com.actionfps.ladder.parser

/**
  * Created by me on 09/01/2017.
  */
case class KeyedAggregate[T](aggregates: Map[T, Aggregate], total: Aggregate) {
  def includeLine(key: T)(timedPlayerMessage: TimedPlayerMessage)(implicit userProvider: UserProvider): KeyedAggregate[T] = {
    val original = aggregates.getOrElse(key, Aggregate.empty)
    val updated = original.includeLine(timedPlayerMessage)
    if ( original == updated ) this else {
      val newAggregates = aggregates.updated(key, updated)
      KeyedAggregate(
        aggregates = newAggregates,
        total = newAggregates.valuesIterator.reduce(_.merge(_))
      )
    }
  }
}

object KeyedAggregate {
  def empty[T]: KeyedAggregate[T] = KeyedAggregate(Map.empty, Aggregate.empty)
}
