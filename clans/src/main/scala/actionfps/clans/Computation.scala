package actionfps.clans

import java.net.URI

import org.apache.http.client.fluent.Request
import org.apache.http.entity.mime.MultipartEntityBuilder
import play.api.libs.json.{Json, JsObject}

import scala.collection.immutable.ListMap
import scala.io.Source

/**
  * Created by William on 26/12/2015.
  */
case class Computation(apiHost: String, phpApiEndpoint: URI) {

  def calculateClanwars(games: List[JsObject]): ClanwarsApi = {
    val meb = MultipartEntityBuilder.create().addTextBody("games", Json.toJson(games).toString).build()
    val res = Request.Post(phpApiEndpoint).body(meb).execute().returnContent().asString()
    Json.fromJson[ClanwarsApi](Json.parse(res)).get
  }

  def calculateClanstats(clanwars: List[JsObject]): Clanstats = {
    val meb = MultipartEntityBuilder.create().addTextBody("clanwars", Json.toJson(clanwars).toString).build()
    val res = Request.Post(phpApiEndpoint).body(meb).execute().returnContent().asString()
    Json.fromJson[Clanstats](Json.parse(res)).get
  }

  def loadAllGames(): ListMap[String, JsObject] = {
    val parseLine = """(.*)\t(.*)""".r
    ListMap(Source.fromInputStream(new URI(s"${apiHost}/all/").toURL.openStream()).getLines().collect {
      case parseLine(id, str) => id -> Json.parse(str).asInstanceOf[JsObject]
    }.toList: _*)
  }

  def loadRecentClangames(): ListMap[String, JsObject] = {
    ListMap(Json.fromJson[List[JsObject]](Json.parse(new URI(s"${apiHost}/recent/clangames/").toURL.openStream())).get
      .map { j => (j \ "id").validate[String].get -> j }
      : _*)
  }

}
