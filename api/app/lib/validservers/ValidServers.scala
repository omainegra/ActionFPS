package lib.validservers

import java.time.ZoneId

import play.api.libs.json.{Reads, JsValue, Json}

/**
  * Created by William on 25/12/2015.
  */
object ValidServers {
  implicit val zidReads = implicitly[Reads[String]].map(ZoneId.of)
  implicit val rvs = Json.reads[ValidServer]

  def fromJson(jsValue: JsValue) = Json.fromJson[Map[String, ValidServer]](jsValue).map(ValidServers.apply)

  def fromResource = {
    val res = Json.parse(getClass.getResourceAsStream("/servers.json"))
    fromJson(res).get
  }

}

case class ValidServer(name: String, timezone: ZoneId, invalid: Option[Boolean]) {
  def isValid = !invalid.contains(true)
}

case class ValidServers(items: Map[String, ValidServer])


