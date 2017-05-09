package com.actionfps.accumulation

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.servers.ValidServers

/**
  * Created by william on 8/5/17.
  */
object ServerValidator {

  implicit class validator(jsonGame: JsonGame)(
      implicit validServers: ValidServers) {
    def validateServer: Boolean = {
      val server = jsonGame.server
      validServers.items.exists(item => item._1 == server && item._2.isValid)
    }
  }

}
