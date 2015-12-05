package lib

import java.io.InputStreamReader
import java.time.ZoneId
import javax.inject.Inject

import af.rr._
import lib.users.{CurrentNickname, User}
import org.apache.http.client.fluent.Request
import play.api.Configuration

/**
  * Created by William on 05/12/2015.
  */
class RecordsReader @Inject()(configuration: Configuration) {

  def getConfigUrlReader(id: String): InputStreamReader = {
    configuration.getString(s"af.csv.$id") match {
      case Some(url) =>
        new InputStreamReader(Request.Get(url).execute().returnContent().asStream())
      case _ =>
        throw new RuntimeException(s"Could not find config option af.csv.$id")
    }
  }

  def videos = VideoRecord.parseRecords(getConfigUrlReader("videos"))

  def servers = ServerRecord.parseRecords(getConfigUrlReader("servers"))

  def headings = HeadingsRecord.parseRecords(getConfigUrlReader("headings"))

  def clans: List[lib.clans.Clan] = ClanRecord.parseRecords(getConfigUrlReader("clans")).map { clan =>
    lib.clans.Clan(
      id = clan.id,
      name = clan.shortName,
      `full name` = clan.longName,
      tag = if (clan.tag2.nonEmpty) None else Option(clan.tag),
      tags = if (clan.tag2.isEmpty) None else Option(List(clan.tag) ++ clan.tag2),
      website = clan.website.map(_.toString)
    )
  }

  def users: List[User] = {
    val registrations = Registration.parseRecords(getConfigUrlReader("registrations"))
    val nicknames = NicknameRecord.parseRecords(getConfigUrlReader("nicknames"))
    for {
      registration <- registrations
      hisNicks = nicknames.filter(_.id == registration.id).sortBy(_.from.toString)
      if hisNicks.nonEmpty
      currentNickname = hisNicks.last
      previousNicknames = hisNicks.dropRight(1)
    } yield User(
      id = registration.id,
      name = registration.name,
      countryCode = None,
      email = registration.email,
      registrationDate = registration.registrationDate.atZone(ZoneId.of("UTC")),
      nickname = CurrentNickname(
        nickname = currentNickname.nickname,
        countryCode = None,
        from = currentNickname.from.atZone(ZoneId.of("UTC"))
      ),
      previousNicknames = None
    )
  }
}
