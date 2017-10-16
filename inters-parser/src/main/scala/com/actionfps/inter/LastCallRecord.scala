package com.actionfps.inter

import java.time.{Duration, Instant}

/**
  * Created by me on 17/01/2017.
  */
case class LastCallRecord(users: Map[String, Instant],
                          servers: Map[String, Instant],
                          ips: Map[String, Instant]) {

  /** Return if updated <=> had effect, so emit a record **/
  def include(interOut: InterOut): Option[LastCallRecord] = {
    import interOut.userMessage._
    val acceptByUser = users.get(userId) match {
      case None => true
      case Some(latestUserInstant) =>
        instant.minus(LastCallRecord.UserTimeout).isAfter(latestUserInstant)
    }
    val acceptByServer = servers.get(serverId) match {
      case None => true
      case Some(latestServerInstant) =>
        instant
          .minus(LastCallRecord.ServerTimeout)
          .isAfter(latestServerInstant)
    }
    val acceptByIp = ips.get(ip) match {
      case None => true
      case Some(latestIpInstant) =>
        instant.minus(LastCallRecord.IpTimeout).isAfter(latestIpInstant)
    }
    if (acceptByServer && acceptByUser && acceptByIp) Option {
      LastCallRecord(
        users = users.updated(userId, instant),
        ips = ips.updated(ip, instant),
        servers = servers.updated(serverId, instant)
      )
    } else None
  }
}

object LastCallRecord {
  val UserTimeout: Duration = java.time.Duration.ofMinutes(5)
  val IpTimeout: Duration = java.time.Duration.ofMinutes(5)
  val ServerTimeout: Duration = java.time.Duration.ofMinutes(5)
  val empty: LastCallRecord = LastCallRecord(
    users = Map.empty,
    servers = Map.empty,
    ips = Map.empty
  )
}
