package providers

import java.io.{StringWriter, StringReader}
import javax.inject.Inject

import af.rr.{NicknameRecord, Registration, ServerRecord}
import af.{User, Clan}
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient


import scala.async.Async._
import scala.concurrent.duration.Duration

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 01/01/2016.
  */
class ReferenceProvider @Inject()(configuration: Configuration, cacheApi: CacheApi)
                                 (implicit wSClient: WSClient,
                                  executionContext: ExecutionContext) {

  import controllers.cf

  def unCache(): Unit = {
    List("clans", "servers", "registrations", "nicknames").foreach(cacheApi.remove)
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

  object Clans {
    def raw: Future[String] = fetch("clans")

    def clans: Future[List[Clan]] = raw.map { bdy =>
      val sr = new StringReader(bdy)
      try af.rr.ClanRecord.parseRecords(sr).map(Clan.fromClanRecord)
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

    def rawRegistrations: Future[String] = fetch("registrations").map(
      bdy => Registration.filterRegistrationsEmail(new StringReader(bdy))
    )

    def registrations: Future[List[Registration]] = rawRegistrations.map { bdy =>
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
  }

  def clans: Future[List[Clan]] = Clans.clans

  def users: Future[List[User]] = Users(withEmails = false).users

  implicit val serverRecordRead = Json.reads[ServerRecord]

  def servers: Future[List[ServerRecord]] = Servers.servers

}
