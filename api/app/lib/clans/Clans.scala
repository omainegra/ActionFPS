package lib.clans

import lib.Yaml
import play.api.libs.json.Json

trait Clans {
  implicit val fmt = Json.format[Clan]
  def json: String = Yaml.toJson(yaml)
  def yaml: String
  def clans: Map[String, Clan] =
    Json.fromJson[Map[String, Clan]](Json.parse(json)).get
}
