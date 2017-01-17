package com.actionfps.inter

import java.time.Instant

import com.actionfps.accumulation.ValidServers
import com.actionfps.accumulation.user.User
import com.actionfps.gameparser.mserver.ExtractMessage

/**
  * Created by me on 17/01/2017.
  */
object InterOut {
  def fromMessage(users: List[User])(message: String)(implicit validServers: ValidServers): Option[InterOut] = {
    message match {
      case ExtractMessage(zdt, validServers.FromLog(server), InterMessage(interMessage)) =>
        for {
          serverAddress <- server.address
          user <- users.find(_.nickname == interMessage.nickname)
        } yield InterOut(
          instant = zdt.toInstant,
          user = user.id,
          playerName = interMessage.nickname,
          serverName = server.name,
          serverConnect = serverAddress
        )
    }
  }
}

case class InterOut(instant: Instant,
                    user: String,
                    playerName: String,
                    serverName: String,
                    serverConnect: String)
