package com.actionfps.user

import java.io.Reader
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.actionfps.user.RegistrationEmail.PlainRegistrationEmail
import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class Registration(id: String,
                        name: String,
                        email: RegistrationEmail,
                        registrationDate: LocalDateTime,
                        currentNickname: String) {
  def toCsvLine: String =
    List(id,
         name,
         email.stringValue,
         Registration.dtf.format(registrationDate),
         currentNickname).mkString(",")

  def secured(implicit publicKey: PublicKey): Registration = email match {
    case p: PlainRegistrationEmail => copy(email = p.secured)
    case o => this
  }
}

object Registration {

  def secure(registrations: List[Registration])(
      implicit publicKey: PublicKey): List[Registration] = {
    registrations.map(_.secured)
  }

  def writeCsv(registrations: List[Registration]): String = {
    (header :: registrations.map(_.toCsvLine)).mkString("\n")
  }

  val dtf: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val dtf2: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
  val idColumn = "ID"
  val currentNicknameColumn = "Current Nickname"
  val nameColumn = "Name"
  val emailColumn = "E-mail"
  val registrationDateColumn = "Registration date"
  val header =
    s"$idColumn,$nameColumn,$emailColumn,$registrationDateColumn,$currentNicknameColumn"

  def parseRecords(input: Reader): List[Registration] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL
      .withHeader()
      .parse(input)
      .asScala
      .flatMap { rec =>
        for {
          id <- Option(rec.get(idColumn)).filter(_.nonEmpty)
          currentNickname <- Option(rec.get(currentNicknameColumn))
            .filter(_.nonEmpty)
          name <- Option(rec.get(nameColumn)).filter(_.nonEmpty)
          email <- Option(rec.get(emailColumn))
            .filter(_.nonEmpty)
            .map(RegistrationEmail.fromString)
            .flatMap(_.right.toOption)
          registrationDate <- Try(
            LocalDateTime.parse(rec.get(registrationDateColumn), dtf))
            .orElse(
              Try(LocalDateTime.parse(rec.get(registrationDateColumn), dtf2)))
            .toOption
        } yield
          Registration(
            id = id,
            name = name,
            email = email,
            registrationDate = registrationDate,
            currentNickname = currentNickname
          )
      }
      .toList
  }
}
