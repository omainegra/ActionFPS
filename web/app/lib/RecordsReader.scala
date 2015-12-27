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
    val url = s"http://odin.duel.gg:59991/${id}/"
    new InputStreamReader(Request.Get(url).execute().returnContent().asStream())
  }

  def fetchServers() = ServerRecord.parseRecords(getConfigUrlReader("servers"))

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
