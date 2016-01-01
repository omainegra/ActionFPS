package af.rr

import java.io.Reader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class Registration(id: String, name: String, email: String, registrationDate: LocalDateTime)

object Registration {
  val dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

  def parseRecords(input: Reader): List[Registration] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        id <- Option(rec.get(0)).filter(_.nonEmpty)
        name <- Option(rec.get(1)).filter(_.nonEmpty)
        email <- Option(rec.get(2)).filter(_.nonEmpty)
        registrationDate <- Try(LocalDateTime.parse(rec.get(3), dtf))
          .orElse(Try(LocalDateTime.parse(rec.get(3), dtf2))).toOption
      } yield Registration(
        id = id,
        name = name,
        email = email,
        registrationDate = registrationDate
      )
    }.toList
  }
}
