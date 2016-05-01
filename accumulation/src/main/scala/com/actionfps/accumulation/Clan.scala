package com.actionfps.accumulation

import com.actionfps.reference.ClanRecord

/**
  * Created by William on 26/12/2015.
  */

case class Clan(id: String, name: String, fullName: String,
                tag: Option[String], tags: Option[List[String]], website: Option[String],
                teamspeak: Option[String],
                logo: String) {
  def nicknameInClan(nickname: String): Boolean = {
    (tag.toList ++ tags.toList.flatten).exists(NicknameMatcher.apply(_)(nickname))
  }
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
      logo = clanRecord.logo.toString,
      teamspeak = clanRecord.teamspeak.map(_.toString)
    )
  }
}
