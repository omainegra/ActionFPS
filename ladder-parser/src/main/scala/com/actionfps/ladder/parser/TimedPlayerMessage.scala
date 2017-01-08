package com.actionfps.ladder.parser

import java.time.ZonedDateTime

/**
  * Created by me on 08/01/2017.
  */
case class TimedPlayerMessage(time: ZonedDateTime, playerMessage: PlayerMessage)
