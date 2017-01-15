package com.actionfps

/**
  * Created by me on 15/01/2017.
  */
package object accumulation {
  private[accumulation] def mostCommon[T](from: List[T]): Option[T] = {
    from
      .groupBy(identity)
      .mapValues(_.size)
      .toList
      .sortBy { case (item, count) => count }
      .lastOption
      .map { case (item, count) => item }
  }
}
