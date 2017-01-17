package com.actionfps.inter

import java.time.Instant

/**
  * Created by me on 17/01/2017.
  */
case class LastCallRecord(user: Map[String, Instant], server: Map[String, Instant]) {
  /** Return if updated <=> had effect, so emit a record **/
  def include(interOut: InterOut): Option[LastCallRecord] = {
    val acceptByUser = user.get(interOut.user) match {
      case None => true
      case Some(latestUserInstant) =>
        interOut.instant.minus(java.time.Duration.ofMinutes(5)).isAfter(latestUserInstant)
    }
    val acceptByServer = server.get(interOut.serverName) match {
      case None => true
      case Some(latestServerInstant) =>
        interOut.instant.minus(java.time.Duration.ofMinutes(5)).isAfter(latestServerInstant)
    }
    if (acceptByServer && acceptByUser) Option {
      LastCallRecord(
        user = user.updated(interOut.user, interOut.instant),
        server = server.updated(interOut.serverName, interOut.instant)
      )
    } else None
  }
}

object LastCallRecord {
  val empty: LastCallRecord = LastCallRecord(
    user = Map.empty,
    server = Map.empty
  )
}
