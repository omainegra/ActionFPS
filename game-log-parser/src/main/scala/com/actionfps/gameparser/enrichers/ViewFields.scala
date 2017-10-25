package com.actionfps.gameparser.enrichers

import java.time.ZonedDateTime

/**
  * Created by me on 29/05/2016.
  */
case class ViewFields(startTime: ZonedDateTime, winner: Option[String], winnerClan: Option[String])

