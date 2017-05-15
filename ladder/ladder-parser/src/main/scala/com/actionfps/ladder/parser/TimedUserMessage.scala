package com.actionfps.ladder.parser

import java.time.Instant

/**
  * Created by me on 08/01/2017.
  */
case class TimedUserMessage(instant: Instant, user: String, userAction: UserAction) {
}

