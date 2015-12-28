package services

/**
  * Created by William on 28/12/2015.
  */

import java.io.StringReader
import javax.inject._

import af.{Clan, User}
import af.rr.{ClanRecord, NicknameRecord, Registration}
import org.apache.commons.csv.{CSVPrinter, CSVFormat}
import org.apache.http.client.fluent.Request
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.EventSource.Event

@Singleton
class RecordsService @Inject()(configuration: Configuration, cacheApi: CacheApi,
                              eventPublisherService: EventPublisherService)(){

  def fetchReference(id: String) = {
    val url = configuration.underlying.getString(s"af.csv.$id")
    var res = Request.Get(url).execute().returnContent().asString()
    if (id == "registrations") {
      import collection.JavaConverters._
      /** Hide e-mails **/
      val cleanRecords = CSVFormat.EXCEL.parse(new StringReader(res)).asScala.zipWithIndex.map {
        case (rec, i) =>
          rec.iterator().asScala.zipWithIndex.map {
            case (value, 2) if i > 0 => ""
            case (value, _) => value
          }.toList
      }.toList
      val b = new java.lang.StringBuilder()
      val p = new CSVPrinter(b, CSVFormat.EXCEL)
      p.printRecords(cleanRecords.map(_.toArray): _*)
      res = b.toString
    }
    res
  }

  def invalidate(): Unit = {
    List("registrations", "clans", "servers", "nicknames").foreach(n =>
      cacheApi.remove(n))

    eventPublisherService.push(Event("reference-updated"))
  }

  def getReference(id: String): String = {
    cacheApi.getOrElse[String](id)(fetchReference(id))
  }

  def users: List[User] = {
    val regs = Registration.parseRecords(new StringReader(getReference("registrations")))
    val nicks = NicknameRecord.parseRecords(new StringReader(getReference("nicknames")))
    regs.flatMap{reg => User.fromRegistration(reg, nicks)}
  }

  def clans: List[Clan] = {
    ClanRecord.parseRecords(new StringReader(getReference("clans"))).map(Clan.fromClanRecord)
  }

}
