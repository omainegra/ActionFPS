package lib
package users

import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import play.api.libs.json.Json

import scala.xml.PCData

trait Users {
  def json: String

  import User.userFormat

  def users: List[User] = Json.fromJson[List[User]](Json.parse(json)).getOrElse(throw new RuntimeException(s"Failed to parse $json"))
}

object BasexUsers extends Users {

  lazy val xQuery = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/get-users-json.xq")).mkString
  lazy val basexQuery = <query xmlns="http://basex.org/rest">
    <text>
      {PCData(xQuery)}
    </text>
  </query>
  override lazy val json: String =
    Request.Post("http://admin:admin@odin.duel.gg:11813/rest/user")
      .bodyString(basexQuery.toString, ContentType.APPLICATION_XML)
      .execute().returnContent().asString()
}