package af

import play.api.libs.json.{JsValue, Json}

/**
  * Created by William on 26/12/2015.
  */

object ValidServers {
  implicit val rvs = Json.reads[ValidServer]

  def fromJson(jsValue: JsValue) = Json.fromJson[Map[String, ValidServer]](jsValue).map(ValidServers.apply)

  def fromResource = {
    val res = Json.parse(getClass.getResourceAsStream("/af/acc/valid-servers.json"))
    fromJson(res).get
  }
}

case class ValidServer(name: String)

case class ValidServers(items: Map[String, ValidServer])
