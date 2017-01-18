package com.actionfps.inter

import java.time.{Duration, Instant}

/**
  * Created by me on 17/01/2017.
  */
case class LastCallRecord(user: Map[String, Instant],
                          server: Map[String, Instant],
                          ip: Map[String, Instant]) {
  /** Return if updated <=> had effect, so emit a record **/
  def include(interOut: InterOut): Option[LastCallRecord] = {
    val acceptByUser = user.get(interOut.user) match {
      case None => true
      case Some(latestUserInstant) =>
        interOut.instant.minus(LastCallRecord.UserTimeout).isAfter(latestUserInstant)
    }
    val acceptByServer = server.get(interOut.serverName) match {
      case None => true
      case Some(latestServerInstant) =>
        interOut.instant.minus(LastCallRecord.ServerTimeout).isAfter(latestServerInstant)
    }
    val acceptByIp = ip.get(interOut.ip) match {
      case None => true
      case Some(latestIpInstant) =>
        interOut.instant.minus(LastCallRecord.IpTimeout).isAfter(latestIpInstant)
    }
    if (acceptByServer && acceptByUser && acceptByIp) Option {
      LastCallRecord(
        user = user.updated(interOut.user, interOut.instant),
        ip = ip.updated(interOut.ip, interOut.instant),
        server = server.updated(interOut.serverName, interOut.instant)
      )
    } else None
  }
}

object LastCallRecord {
  val UserTimeout: Duration = java.time.Duration.ofMinutes(5)
  val IpTimeout: Duration = java.time.Duration.ofMinutes(5)
  val ServerTimeout: Duration = java.time.Duration.ofMinutes(5)
  val empty: LastCallRecord = LastCallRecord(
    user = Map.empty,
    server = Map.empty,
    ip = Map.empty
  )
}
