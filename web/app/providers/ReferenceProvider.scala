package providers

import java.io.StringReader
import javax.inject.{Inject, Singleton}

import com.actionfps.accumulation.Clan
import com.actionfps.reference._
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import com.actionfps.formats.json.Formats._
import com.actionfps.user.User

/**
  * Created by William on 01/01/2016.
  *
  * Provides reference data from CSV URLs.
  */
@Singleton
class ReferenceProvider @Inject()(configuration: Configuration,
                                  cacheApi: CacheApi)
                                 (implicit wSClient: WSClient,
                                  executionContext: ExecutionContext) {

  import ReferenceProvider._

  def unCache(): Unit = {
    List(ClansKey, ServersKey, RegistrationsKey, NicknamesKey).foreach(cacheApi.remove)
  }

  private def fetch(key: String) = async {
    cacheApi.get[String](key) match {
      case Some(value) => value
      case None =>
        val url = configuration.underlying.getString(s"af.reference.${key}")
        val value = await(wSClient.url(url).get()) match {
          case r if r.status == 200 => r.body
          case other => throw new RuntimeException(s"Received unexpected response ${other.status} for ${url}")
        }
        cacheApi.set(key, value, Duration.apply("1h"))
        value
    }
  }

  object Clans {
    private def csv: Future[String] = fetch(ClansKey)

    def clans: Future[List[Clan]] = csv.map { bdy =>
      Json.parse(bdy).validate[List[Clan]].map(_.filter(_.valid)).getOrElse{
        throw new RuntimeException(s"Failed to parse JSON from body: ${bdy}")
      }
    }
  }

  object Servers {
    private def raw: Future[String] = fetch(ServersKey)

    def servers: Future[List[ServerRecord]] = raw.map { bdy =>
      val sr = new StringReader(bdy)
      try ServerRecord.parseRecords(sr)
      finally sr.close()
    }
  }

  object Users {

    def registrations: Future[List[Registration]] = fetch(RegistrationsKey).map { bdy =>
      val sr = new StringReader(bdy)
      try Registration.parseRecords(sr)
      finally sr.close()
    }

    private def rawNicknames: Future[String] = fetch(NicknamesKey)

    def nicknames: Future[List[NicknameRecord]] = rawNicknames.map { bdy =>
      val sr = new StringReader(bdy)
      try NicknameRecord.parseRecords(sr)
      finally sr.close()
    }

    def users: Future[List[User]] = async {
      val regs = await(registrations)
      val nicks = await(nicknames)
      regs.flatMap { reg => User.fromRegistration(reg, nicks) }
    }

  }

  def clans: Future[List[Clan]] = Clans.clans

  def users: Future[List[User]] = Users.users

  private implicit val serverRecordRead = Json.reads[ServerRecord]

  def servers: Future[List[ServerRecord]] = Servers.servers

}

object ReferenceProvider {
  val ClansKey = "clans"
  val ServersKey = "servers"
  val RegistrationsKey = "registrations"
  val NicknamesKey = "nicknames"
}
