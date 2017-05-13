package com.actionfps.ladder.parser

/**
  * Created by me on 09/01/2017.
  */
case class KeyedAggregate[T](aggregates: Map[T, Aggregate]) {
  def total: Aggregate = aggregates.valuesIterator.reduce(_.merge(_))

  def includeAggregate(key: T)(aggregate: Aggregate): KeyedAggregate[T] = {
    aggregates.get(key) match {
      case Some(`aggregate`) => this
      case _ =>
        KeyedAggregate(
          aggregates = aggregates.updated(key, aggregate)
        )
    }
  }

  def includeLine(key: T)(tmu: TimedUserMessage): KeyedAggregate[T] = {
    includeAggregate(key)(
      aggregates.getOrElse(key, Aggregate.empty).includeLine(tmu))
  }
}

object KeyedAggregate {
  def empty[T]: KeyedAggregate[T] = KeyedAggregate(aggregates = Map.empty)
}
