package com.actionfps.reference

import java.io.Reader
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class ClanRecord(id: String, shortName: String, longName: String, website: Option[URI], tag: String, tag2: Option[String],
                      logo: URI, teamspeak: Option[URI])

object ClanRecord {

  def parseRecords(input: Reader): List[ClanRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.map { rec =>
      for {
        id <- Option(rec.get(0)).filter(_.nonEmpty)
        shortName <- Option(rec.get(1)).filter(_.nonEmpty)
        longName <- Option(rec.get(2)).filter(_.nonEmpty)
        website = Try(new URI(rec.get(3))).toOption.filter(_.toString.nonEmpty)
        tag <- Option(rec.get(4)).filter(_.nonEmpty)
        tag2 = Try(rec.get(5)).toOption.filter(_ != null).filter(_.nonEmpty)
        logo <- Try(Option(new URI(rec.get(6)))
          .filter(u => u.getPath.endsWith(".png") || u.getPath.endsWith(".svg") || u.getHost.contains("github"))
          .filter(u => u.getScheme == "https")
        )
          .toOption.flatten
        teamspeak = Try(Option(new URI(rec.get(7)))).toOption.flatten.filter(_.getScheme == "ts3server")
      } yield ClanRecord(
        id = id,
        shortName = shortName,
        longName = longName,
        website = website,
        tag = tag,
        tag2 = tag2,
        logo = logo,
        teamspeak = teamspeak
      )
    }.flatten.toList

  }
}