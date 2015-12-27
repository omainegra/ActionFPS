package controllers

import java.io.{StringReader, InputStreamReader}

import af.{User, Clan}
import af.rr.ServerRecord
import org.apache.commons.csv.{CSVPrinter, CSVFormat}
import org.apache.http.client.fluent.Request
import play.api.Configuration
import play.api.libs.json.Json
import javax.inject._

import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext
import play.api.cache._

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class RecordsController @Inject()(configuration: Configuration, cached: Cached)
                                 (implicit executionContext: ExecutionContext) extends Controller {

  def getConfigUrlReader(id: String) = {
    configuration.getString(s"af.csv.$id") match {
      case Some(url) =>
        Request.Get(url).execute().returnContent().asStream()
      case _ =>
        throw new RuntimeException(s"Could not find config option af.csv.$id")
    }
  }

  def referenceEndpoint(id: String) = cached(id) {
    Action {
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
      Ok(res)
    }
  }

  def registrations = referenceEndpoint("registrations")

  def clans = referenceEndpoint("clans")

  def servers = referenceEndpoint("servers")

  def nicknames = referenceEndpoint("nicknames")

}
