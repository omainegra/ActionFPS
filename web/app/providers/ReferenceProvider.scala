package providers

import java.io.StringReader

import com.actionfps.accumulation.Clan
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import com.actionfps.formats.json.Formats._
import com.actionfps.servers.ServerRecord
import com.actionfps.user.{NicknameRecord, Registration, User}
import controllers.{
  ProvidesClanNames,
  ProvidesServers,
  ProvidesUsers,
  ProvidesUsersList
}
import lib.ClansProvider

/**
  * Created by William on 01/01/2016.
  *
  * Provides reference data from CSV URLs.
  */
final class ReferenceProvider(configuration: Configuration,
                              cacheApi: AsyncCacheApi)(
    implicit wSClient: WSClient,
    executionContext: ExecutionContext)
    extends ProvidesServers
    with ProvidesUsers
    with ClansProvider
    with ProvidesClanNames
    with ProvidesUsersList {

  import ReferenceProvider._

  def unCache(): Unit = {
    List(ClansKey, ServersKey, RegistrationsKey, NicknamesKey, UsersKey)
      .foreach(cacheApi.remove)
  }

  private def fetch(key: String) = async {
    await(cacheApi.get[String](key)) match {
      case Some(value) => value
      case None =>
        val url = configuration.underlying.getString(s"af.reference.${key}")
        val value = await(wSClient.url(url).get()) match {
          case r if r.status == 200 => r.body
          case other =>
            throw new RuntimeException(
              s"Received unexpected response ${other.status} for ${url}")
        }
        await(cacheApi.set(key, value, Duration.apply("1h")))
        value
    }
  }

  object Clans {
    private def csv: Future[String] = fetch(ClansKey)

    def clans: Future[List[Clan]] = csv.map { bdy =>
      Json.parse(bdy).validate[List[Clan]].map(_.filter(_.valid)).getOrElse {
        throw new RuntimeException(s"Failed to parse JSON from body: ${bdy}")
      }
    }
  }

  object Servers {
    private def raw: Future[String] = fetch(ServersKey)

    def servers: Future[List[ServerRecord]] = raw.map { bdy =>
      Json.parse(bdy).validate[List[ServerRecord]].getOrElse {
        throw new RuntimeException(s"Failed to parse JSON from body: ${bdy}")
      }
    }
  }

  object Users {

    def registrations: Future[List[Registration]] =
      fetch(RegistrationsKey).map { bdy =>
        val sr = new StringReader(bdy)
        try Registration.parseRecords(sr)
        finally sr.close()
      }

    private def rawNicknames: Future[String] = fetch(NicknamesKey)

    private def nicknames: Future[List[NicknameRecord]] = rawNicknames.map {
      bdy =>
        val sr = new StringReader(bdy)
        try NicknameRecord.parseRecords(sr)
        finally sr.close()
    }

    def users: Future[List[User]] =
      cacheApi.getOrElseUpdate[List[User]](UsersKey) {
        async {
          val regs = await(registrations)
          val nicks = await(nicknames)
          regs.flatMap { reg =>
            User.fromRegistration(reg, nicks)
          }
        }
      }

  }

  def clans: Future[List[Clan]] = Clans.clans

  def users: Future[List[User]] = Users.users

  private implicit val serverRecordRead = Json.reads[ServerRecord]

  def servers: Future[List[ServerRecord]] = Servers.servers

  override def clanNames: Future[Map[String, String]] =
    clans.map(_.map(c => c.id -> c.name).toMap)
}

object ReferenceProvider {
  val ClansKey = "clans"
  val ServersKey = "servers"
  val RegistrationsKey = "registrations"
  val NicknamesKey = "nicknames"

  val UsersKey = "users"

}
