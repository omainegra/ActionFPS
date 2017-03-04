package com.actionfps.reference

import java.io.Reader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.actionfps.reference.Registration.Email
import com.actionfps.reference.Registration.Email.SecureEmail
import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class Registration(id: String, name: String, email: Email, registrationDate: LocalDateTime, currentNickname: String) {
  def toCsvLine: String = List(id, name, email.stringValue, Registration.dtf.format(registrationDate), currentNickname).mkString(",")

  def withSecureEmail: Registration = copy(email = email.secured)
}

object Registration {

  def writeCsv(registrations: List[Registration]): String = {
    (header :: registrations.map(_.toCsvLine)).mkString("\n")
  }

  sealed trait Email {
    def matches(emailString: String): Boolean

    def stringValue: String

    def secured: SecureEmail
  }

  object Email {

    final case class PlainEmail(email: String) extends Email {
      override def stringValue: String = s"$mailto$email"

      override def matches(emailString: String): Boolean = emailString == email

      override def secured: SecureEmail = SecureEmail(email)
    }

    final case class SecureEmail(encrypted: String) extends Email {
      override def stringValue: String = s"$secureEmailPrefix$encrypted"

      override def matches(emailString: String): Boolean = encrypted == emailString

      override def secured: SecureEmail = this
    }

    val mailto = "mailto:"
    val secureEmailPrefix = "uri:actionfps.com:email:"

    def fromString(string: String): Either[String, Email] = {
      if (string.startsWith(secureEmailPrefix))
        Right(SecureEmail(string.drop(secureEmailPrefix.length)))
      else if (string.startsWith(mailto))
        Right(PlainEmail(string.drop(mailto.length)))
      else if (string.contains('@'))
        Right(PlainEmail(string))
      else Left("Could not parse e-mail")
    }
  }

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val dtf2: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
  val idColumn = "ID"
  val currentNicknameColumn = "Current Nickname"
  val nameColumn = "Name"
  val emailColumn = "E-mail"
  val registrationDateColumn = "Registration date"
  val header = s"$idColumn,$nameColumn,$emailColumn,$registrationDateColumn,$currentNicknameColumn"

  def parseRecords(input: Reader): List[Registration] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.withHeader().parse(input).asScala.flatMap { rec =>
      for {
        id <- Option(rec.get(idColumn)).filter(_.nonEmpty)
        currentNickname <- Option(rec.get(currentNicknameColumn)).filter(_.nonEmpty)
        name <- Option(rec.get(nameColumn)).filter(_.nonEmpty)
        email <- Option(rec.get(emailColumn)).filter(_.nonEmpty).map(Email.fromString).flatMap(_.right.toOption)
        registrationDate <- Try(LocalDateTime.parse(rec.get(registrationDateColumn), dtf))
          .orElse(Try(LocalDateTime.parse(rec.get(registrationDateColumn), dtf2))).toOption
      } yield Registration(
        id = id,
        name = name,
        email = email,
        registrationDate = registrationDate,
        currentNickname = currentNickname
      )
    }.toList
  }
}
