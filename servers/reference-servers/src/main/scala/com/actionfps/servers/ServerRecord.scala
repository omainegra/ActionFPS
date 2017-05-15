package com.actionfps.servers

/**
  * Created by William on 05/12/2015.
  */
case class ServerRecord(region: String,
                        hostname: String,
                        port: Int,
                        kind: String,
                        password: Option[String]) {
  def address = s"""$hostname:$port"""

  def connectUrl: String = {
    val pwdBit = password.filter(_.nonEmpty).map { password =>
      s"?password=$password"
    }
    s"${protocol}://${hostname}:${port}${pwdBit.getOrElse("")}"
  }

  def joinUrl: String = {
    s"/servers/?join=${hostname}:${port}"
  }

  def name: String = s"${hostname} ${port}"

  def protocol: String =
    if (kind == "ActionFPS") "actionfps" else "assaultcube"

}
