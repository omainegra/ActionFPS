package services

/**
  * Created by William on 25/12/2015.
  */
import javax.inject._

import af.ValidServers._
import play.api.libs.json.Json

@Singleton
class ValidServersService {
  def fromResource = {
    val res = Json.parse(getClass.getResourceAsStream("/servers.json"))
    fromJson(res).get
  }
  val validServers = fromResource
}
