package com.actionfps.accumulation

import java.time.ZoneId

import com.actionfps.gameparser.enrichers.JsonGame
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by William on 26/12/2015.
  */

object ValidServers {
  implicit val zidReads = implicitly[Reads[String]].map(ZoneId.of)
  implicit val rvs = Json.reads[ValidServer]

  def fromJson(jsValue: JsValue) = Json.fromJson[Map[String, ValidServer]](jsValue).map(ValidServers.apply)

  def fromResource = {
    val res = Json.parse(getClass.getResourceAsStream("valid-servers.json"))
    fromJson(res).get
  }

  object ImplicitValidServers {
    implicit val fromResourceVS = fromResource
  }

  object Validator {

    implicit class validator(jsonGame: JsonGame)(implicit validServers: ValidServers) {
      def validateServer: Boolean = {
        val server = jsonGame.server
        validServers.items.exists(item => item._1 == server && item._2.isValid)
      }
    }

  }

}

case class ValidServer(name: String, timezone: ZoneId, invalid: Option[Boolean], address: Option[String]) {
  def isValid = !invalid.contains(true)
}

case class ValidServers(items: Map[String, ValidServer]) {

  object FromLog {
    def unapply(string: String): Option[ValidServer] = items.get(string)
  }

  object AddressFromLog {
    def unapply(string: String): Option[String] = items.get(string).flatMap(_.address)
  }

}


