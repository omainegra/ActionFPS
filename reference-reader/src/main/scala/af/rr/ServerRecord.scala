package af.rr

import java.io.Reader
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */

case class ServerRecord(region: String, hostname: String, port: Int, kind: String, password: Option[String])

object ServerRecord {
  def parseRecords(input: Reader): List[ServerRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        region <- Option(rec.get(0))
        hostname <- Option(rec.get(1)).filter(_.nonEmpty)
        port <- Try(rec.get(2).toInt).toOption
        kind <- Option(rec.get(3)).filter(_.nonEmpty)
        password <- Option(rec.get(4))
      } yield ServerRecord(
        region = region,
        hostname = hostname,
        port = port,
        kind = kind,
        password = Option(password)
      )
    }.toList

  }
}
