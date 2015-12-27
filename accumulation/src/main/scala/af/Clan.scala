package af

import af.rr.ClanRecord
import play.api.libs.json.Json

/**
  * Created by William on 26/12/2015.
  */

case class Clan(id: String, name: String, fullName: String,
                tag: Option[String], tags: Option[List[String]], website: Option[String],
                logo: Option[String]) {
  def nicknameInClan(nickname: String): Boolean = {
    (tag.toList ++ tags.toList.flatten).exists(NicknameMatcher.apply(_)(nickname))
  }
  implicit val cw = Json.writes[Clan]
  def toJson = Json.toJson(this)
}
object Clan {

  def fromClanRecord(clanRecord: ClanRecord): Clan = {
    Clan(
      id = clanRecord.id,
      name = clanRecord.shortName,
      fullName = clanRecord.longName,
      tag = if (clanRecord.tag2.nonEmpty) None else Option(clanRecord.tag),
      tags = if (clanRecord.tag2.isEmpty) None else Option(List(clanRecord.tag) ++ clanRecord.tag2),
      website = clanRecord.website.map(_.toString),
      logo = clanRecord.logo.map(_.toString)
    )
  }
}
