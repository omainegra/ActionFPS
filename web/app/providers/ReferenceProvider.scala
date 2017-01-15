package providers

import java.io.StringReader
import javax.inject.{Inject, Singleton}

import com.actionfps.accumulation.{Clan, User}
import com.actionfps.ladder.parser.UserProvider
import com.actionfps.reference._
import com.google.common.io.CharStreams
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

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

  def unCache(): Unit = {
    List("clans", "servers", "registrations", "nicknames", "user-provider").foreach(cacheApi.remove)
  }

  private def fetch(key: String) = async {
    cacheApi.get[String](key) match {
      case Some(value) => value
      case None =>
        val value = await(wSClient.url(configuration.underlying.getString(s"af.reference.${key}")).get().filter(_.status == 200).map(_.body))
        cacheApi.set(key, value, Duration.apply("1h"))
        value
    }
  }

  /**
    * Used for [[controllers.LadderController]]
    * @todo remove
    */
  def syncUserProvider(atMost: Duration): UserProvider = cacheApi.getOrElse[UserProvider]("user-provider") {
    Await.result(Users(withEmails = false).provider, atMost)
  }

  object Clans {
    def csv: Future[String] = fetch("clans")

    def clans: Future[List[Clan]] = csv.map { bdy =>
      val sr = new StringReader(bdy)
      try ClanRecord.parseRecords(sr).map(Clan.fromClanRecord)
      finally sr.close()
    }
  }

  object Servers {
    def raw: Future[String] = fetch("servers")

    def servers: Future[List[ServerRecord]] = raw.map { bdy =>
      val sr = new StringReader(bdy)
      try ServerRecord.parseRecords(sr)
      finally sr.close()
    }
  }

  case class Users(withEmails: Boolean = false) {

    def filteredRegistrations: Future[String] = fetch("registrations").map(
      bdy => Registration.filterRegistrationsEmail(new StringReader(bdy))
    )

    protected def rawRegistrations: Future[String] = fetch("registrations").map(
      bdy => CharStreams.toString(new StringReader(bdy))
    )

    def postEmailRegistrations: Future[String] = if (withEmails) rawRegistrations else filteredRegistrations

    def registrations: Future[List[Registration]] = postEmailRegistrations.map { bdy =>
      val sr = new StringReader(bdy)
      try Registration.parseRecords(sr)
      finally sr.close()
    }

    def rawNicknames: Future[String] = fetch("nicknames")

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

    def provider: Future[UserProvider] = users.map(l => new ReferenceProvider.ListUserProvider(l))
  }

  def clans: Future[List[Clan]] = Clans.clans

  def users: Future[List[User]] = Users(withEmails = false).users

  private implicit val serverRecordRead = Json.reads[ServerRecord]

  def servers: Future[List[ServerRecord]] = Servers.servers

}

object ReferenceProvider {

  class ListUserProvider(users: List[User]) extends UserProvider {
    private val nick2UserId = users.map { u => u.nickname.nickname -> u.id }.toMap

    override def username(nickname: String): Option[String] = {
      nick2UserId.get(nickname)
    }
  }

}
