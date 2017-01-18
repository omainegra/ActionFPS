package com.actionfps.inter

import java.time.Instant

/**
  * Created by me on 18/01/2017.
  */
case class UserMessage(instant: Instant,
                       serverId: UserMessage.ServerId,
                       ip: UserMessage.Ip,
                       userId: String,
                       nickname: String,
                       messageText: String) {
  def interOut: Option[InterOut] = {
    if (messageText == "!inter") Some(InterOut(this))
    else None
  }
}

object UserMessage {
  type ServerId = String
  type Ip = String
}
