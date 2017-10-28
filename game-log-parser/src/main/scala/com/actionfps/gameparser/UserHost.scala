package com.actionfps.gameparser

import fastparse.all._
import fastparse.core.Parser

/**
  * Created by me on 04/02/2017.
  */
object UserHost {

  private val ipPart = CharIn('0' to '9').rep(min = 1)

  private val parseHost = (ipPart ~ ("." ~ ipPart).rep(3)).!

  private val userPart = CharIn('a' to 'z').rep(min = 1)

  private val groupPart = userPart

  val parser: Parser[UserHost, Char, String] = {
    parseHost.! ~ (":" ~ userPart.! ~ (":" ~ groupPart.!).?).?
  }.map {
    case (host, None) => OnlyHost(host)
    case (host, Some((user, groupO))) => AuthHost(host, user, groupO)
  }

  def parse(input: String): Option[UserHost] = {
    PartialFunction.condOpt(parser.parse(input)) {
      case Parsed.Success(r, _) => r
    }
  }

  case class OnlyHost(host: String) extends UserHost {
    override def userO: Option[String] = None

    override def group: Option[String] = None
  }

  case class AuthHost(host: String, user: String, group: Option[String])
      extends UserHost {
    override def userO: Option[String] = Some(user)
  }

}

sealed trait UserHost {
  def host: String
  def userO: Option[String]
  def group: Option[String]
}
