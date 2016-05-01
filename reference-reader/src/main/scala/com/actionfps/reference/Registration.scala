package com.actionfps.reference

import java.io.{StringWriter, StringReader, Reader}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.commons.csv.{CSVPrinter, CSVParser, CSVFormat}

import scala.util.Try

case class Registration(id: String, name: String, email: Option[String], registrationDate: LocalDateTime)

object Registration {
  val dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

  def filterRegistrationsEmail(sr: Reader) = {
    val parser = new CSVParser(sr, CSVFormat.EXCEL.withHeader())
    val sw = new StringWriter()

    try {
      import collection.JavaConverters._
      val emailColumn: Int = parser.getHeaderMap.get("E-mail")
      val headers = parser.getHeaderMap.asScala.toList.sortBy(_._2).map(_._1)
      val cp = new CSVPrinter(sw, CSVFormat.EXCEL.withHeader(headers: _*))
      try {
        parser.getRecords.asScala.foreach { csvRecord =>
          val row = csvRecord.iterator().asScala.zipWithIndex.map {
            case (value, `emailColumn`) => ""
            case (value, _) => value
          }.toList
          cp.printRecord(row: _*)
        }
        sw.toString
      } finally cp.close()
    } finally parser.close()
  }

  def parseRecords(input: Reader): List[Registration] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.withHeader().parse(input).asScala.flatMap { rec =>
      for {
        id <- Option(rec.get("ID")).filter(_.nonEmpty)
        name <- Option(rec.get("Name")).filter(_.nonEmpty)
        email = Option(rec.get("E-mail")).filter(_.nonEmpty)
        registrationDate <- Try(LocalDateTime.parse(rec.get("Registration date"), dtf))
          .orElse(Try(LocalDateTime.parse(rec.get("Registration date"), dtf2))).toOption
      } yield Registration(
        id = id,
        name = name,
        email = email,
        registrationDate = registrationDate
      )
    }.toList
  }
}
