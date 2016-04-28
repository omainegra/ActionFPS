package com.actionfps.players

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by William on 2016-04-22.
  */
class UserTimeCounterTest
  extends FunSuite
    with Matchers {

  test("It works") {
    /**
      * Show time for the player relative to UTC
      * For past 50 games
      */
    val input = List(
      "2016-01-01T01:01:01Z",
      "2016-01-01T01:02:02Z",
      "2016-01-01T02:02:02Z",
      "2016-01-10T09:02:02Z",
      "2016-01-10T18:02:02Z"
    )

    val us = input.foldLeft(UserTimeCounter.empty(4))(_.include(_))
    us.pastFifty shouldBe Vector(1, 2, 9, 18)
    us.counts shouldBe Map(1 -> 1, 2 -> 1, 9 -> 1, 18 -> 1)
  }

}