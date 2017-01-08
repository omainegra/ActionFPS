package com.actionfps.ladder.parser

/**
  * Created by me on 09/01/2017.
  */
case class KeyedAggregate[T](aggregates: Map[T, Aggregate], total: Aggregate) {
  def includeLine(key: T)(timedPlayerMessage: TimedPlayerMessage)(implicit userProvider: UserProvider): KeyedAggregate[T] = {
    val newAggregates = aggregates.updated(key, aggregates.getOrElse(key, Aggregate.empty).includeLine(timedPlayerMessage))
    KeyedAggregate(
      aggregates = newAggregates,
      total = newAggregates.valuesIterator.reduce(_.merge(_))
    )
  }
}

object KeyedAggregate {
  def empty[T]: KeyedAggregate[T] = KeyedAggregate(Map.empty, Aggregate.empty)
}
