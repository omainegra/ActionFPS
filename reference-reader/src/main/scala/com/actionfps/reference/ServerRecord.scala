package com.actionfps.reference

import java.io.Reader
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */

case class ServerRecord(region: String, hostname: String, port: Int, kind: String, password: Option[String]) {
  def address = s"""$hostname:$port"""

  def connectAddress = s"""assaultcube://$hostname:$port""" + (password.map(pw => s"/?password=$pw").getOrElse(""))
}

object ServerRecord {
  def parseRecords(input: Reader): List[ServerRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        region <- Try(Option(rec.get(0))).toOption.flatten
        hostname <- Try(Option(rec.get(1))).toOption.flatten.filter(_.nonEmpty)
        port <- Try(rec.get(2).toInt).toOption
        kind <- Try(Option(rec.get(3))).toOption.flatten.filter(_.nonEmpty)
        password = Try(Option(rec.get(4))).toOption.flatten.filter(_.nonEmpty)
      } yield ServerRecord(
        region = region,
        hostname = hostname,
        port = port,
        kind = kind,
        password = password
      )
    }.toList

  }
}
