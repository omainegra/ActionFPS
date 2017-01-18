package com.actionfps.inter

import java.time.Instant

import com.actionfps.accumulation.ValidServers
import com.actionfps.gameparser.mserver.ExtractMessage

/**
  * Created by me on 17/01/2017.
  */
object InterOut {
  def fromEvent(nickToUser: String => Option[String])
               (event: String)
               (implicit validServers: ValidServers): Option[InterOut] = {
    event match {
      case ExtractMessage(zdt, validServers.FromLog(server), InterMessage(interMessage)) =>
        for {
          serverAddress <- server.address
          user <- nickToUser(interMessage.nickname)
        } yield InterOut(
          instant = zdt.toInstant,
          user = user,
          playerName = interMessage.nickname,
          ip = interMessage.ip,
          serverName = server.name,
          serverConnect = serverAddress
        )
      case _ => None
    }
  }
}

case class InterOut(instant: Instant,
                    user: String,
                    playerName: String,
                    serverName: String,
                    serverConnect: String,
                    ip: String)
