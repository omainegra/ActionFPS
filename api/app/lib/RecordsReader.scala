package lib

import java.io.InputStreamReader
import javax.inject.Inject

import af.rr._
import af.{Clan, User}
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

  def fetchVideos() = VideoRecord.parseRecords(getConfigUrlReader("videos"))

  def fetchServers() = ServerRecord.parseRecords(getConfigUrlReader("servers"))

  def fetchHeadings() = HeadingsRecord.parseRecords(getConfigUrlReader("headings"))

  def fetchClans(): List[Clan] = ClanRecord.parseRecords(getConfigUrlReader("clans")).map { clanRecord =>
    Clan.fromClanRecord(clanRecord)
  }

  def fetchUsers(): List[User] = {
    val registrations = Registration.parseRecords(getConfigUrlReader("registrations"))
    val nicknames = NicknameRecord.parseRecords(getConfigUrlReader("nicknames"))
    registrations.flatMap { registration =>
      User.fromRegistration(registration, nicknames)
    }
  }
}
