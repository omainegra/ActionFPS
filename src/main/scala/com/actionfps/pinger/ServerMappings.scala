package com.actionfps.pinger

/**
  * Created by me on 29/05/2016.
  *
  * Server mappings map from IP to hostname.
  * We must do this because the responses don't contain the host names any more.
  *
  * Another approach is to include some sort of signature when sending a message out
  * as the server I believe responds with payload you have sent to it.
  */
trait ServerMappings {
  /**
    * e.g. 1.2.3.4 -> aura.woop.ac
    */
  def connects: Map[String, String]

  /**
    * e.g. 1.2.3.4 -> Aura
    *
    * @return
    */
  def shortNames: Map[String, String]
}

object ServerMappings {
  implicit val default = DefaultServerMappings
}

object DefaultServerMappings extends ServerMappings {
  val connects = Map("62.210.131.155" -> "aura.woop.ac", "104.219.54.14" -> "tyr.woop.ac",
    "104.236.35.55" -> "ny.weed-lounge.me",
    "104.255.33.235" -> "la.weed-lounge.me", "192.184.63.69" -> "califa.actionfps.com",
    "176.126.69.152" -> "bonza.actionfps.com", "191.96.4.147" -> "legal.actionfps.com",
    "150.107.152.50" -> "lah.actionfps.com")
  val shortNames = Map("62.210.131.155" -> "Aura", "104.219.54.14" -> "Tyr",
    "104.236.35.55" -> "NY Lounge", "104.255.33.235" -> "LA Lounge", "192.184.63.69" -> "Califa",
    "176.126.69.152" -> "Bonza", "191.96.4.147" -> "Legal",
    "150.107.152.50" -> "Lah"
  )
}
