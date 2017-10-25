package com.actionfps.gameparser.ingesters

import com.actionfps.gameparser.{SharedParsers, UserHost}
import fastparse.all._

object GameStartHeader {
  private val timeRemaining = CharIn('0' to '9').rep(1).!.map(_.toInt) ~ " " ~ ("minutes" | "minute")

  private val players = CharIn('0' to '9').rep(1).!.map(_.toInt) ~ " players"

  private val cap = P("Game start: ") ~ SharedParsers.acceptedModeParser ~ " on " ~ SharedParsers.mapNameParser ~
    ", " ~ players ~ ", " ~ timeRemaining

  def unapply(input: String): Option[GameStartHeader] = {
    val res = cap.map(Function.tupled(GameStartHeader.apply)).parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(r, _) => r
    }
  }
}

case class GameStartHeader(mode: GameMode.GameMode, map: String, players: Int, minutes: Int)

/**
  * See tests for example messages.
  */
case class GameFinishedHeader(mode: GameMode.GameMode, map: String, state: String)

object GameFinishedHeader {

  private val cap = P("Game status: ") ~ SharedParsers.acceptedModeParser ~ " on " ~ SharedParsers.mapNameParser.! ~
    ", game finished, " ~ CharsWhile(_ != ',').! ~ ", " ~ SharedParsers.digitParser.rep ~ " clients"

  private val capEx = cap.map(Function.tupled(GameFinishedHeader.apply))

  def unapply(input: String): Option[GameFinishedHeader] = {
    val res = capEx.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(r, _) => r
    }
  }
}

case class GameInProgressHeader(mode: GameMode.GameMode, remaining: Int, map: String, state: String)

object GameInProgressHeader {

  private val timeRemaining = CharIn('0' to '9').rep(1).!.map(_.toInt) ~ " " ~ ("minutes" | "minute") ~ " remaining"

  private val clients = CharIn('0' to '9').rep(1).!.map(_.toInt) ~ " clients"

  private val spaces = " ".rep(1)

  /**
    * @example "Game status: hunt the flag on ac_depot, 14 minutes remaining, open, 4 clients"
    **/
  private val cap = P("Game status:") ~ spaces ~ SharedParsers.acceptedModeParser ~ " on " ~ SharedParsers.mapNameParser.! ~
    ", " ~ timeRemaining ~ "," ~ spaces ~ CharsWhile(_ != ',').rep(1).! ~ ", " ~ clients

  private val cap2 = cap.map { case (mode, map, remain, state, clientCount) => GameInProgressHeader(mode, remain, map, state) }

  def unapply(input: String): Option[GameInProgressHeader] = {
    val res = cap2.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(x, _) => x
    }
  }
}

object VerifyTableHeader {
  private val parser = P("cn") ~ " ".rep ~ "name" ~ " ".rep ~ AnyChar.rep

  def unapply(input: String): Boolean = {
    parser.parse(input).isInstanceOf[Parsed.Success[Unit]]
  }
}

object TeamModes {


  private val dig = CharIn('0' to '9')
  private val sp = " ".rep(1)
  private val usp = CharsWhile(_ != ' ').!
  private val strSp = usp ~ " ".rep(1)
  private val dsp = ("-".? ~ dig.rep(1)).!.map(_.toInt)
  private val dSp = dsp ~ " ".rep(1)

  object FragStyle {

    case class IndividualScore(cn: Int, name: String, team: String, score: Int, frag: Int, death: Int, tk: Int, ping: Int, role: String, host: String, user: Option[String], group: Option[String]) extends CreatesGenericIndividualScore {
      override def project: GenericIndividualScore =
        GenericIndividualScore(name, team, None, Option(score), frag, death, Option(host), user, group)
    }

    object IndividualScore {

      private val cap = " ".? ~ dSp ~ strSp ~ strSp ~ dSp ~ dSp ~ dSp ~ dSp ~ dSp ~ strSp ~ UserHost.parser ~ " ".rep
      private val cpp = cap.map { case (a, b, c, d, e, f, g, h, i, j) => IndividualScore(a, b, c, d, e, f, g, h, i, j.host, j.userO, j.group) }

      def unapply(input: String): Option[IndividualScore] = {
        val res = cpp.parse(input)
        PartialFunction.condOpt(res) {
          case Parsed.Success(r, _) => r
        }
      }
    }

    case class IndividualScoreDisconnected(name: String, team: String, frag: Int, death: Int) extends CreatesGenericIndividualScore {
      override def project: GenericIndividualScore = GenericIndividualScore(
        name, team, None, None, frag, death, None, None, None // todo figure out how to get user & group for disconnected.
      )
    }

    object IndividualScoreDisconnected {
      private val isdp = " ".rep ~ strSp ~ strSp ~ dSp ~ dSp ~ " ".rep ~ "-" ~ " ".rep ~ "-" ~ " ".rep ~ "disconnected"
      private val isd = isdp.map { case (a, b, c, d) => IndividualScoreDisconnected(a, b, c, d) }

      def unapply(input: String): Option[IndividualScoreDisconnected] = {
        val rs = isd.parse(input)
        PartialFunction.condOpt(rs) {
          case Parsed.Success(r, _) => r
        }
      }
    }

    case class TeamScore(teamName: String, players: Int, frags: Int) extends CreatesGenericTeamScore {
      override def project: GenericTeamScore = GenericTeamScore(teamName, players, None, frags)
    }

    object TeamScore {

      private val tpp = P("Team") ~ sp ~ CharsWhile(_ != ':').! ~ ":" ~ sp ~ dSp ~ "players," ~ sp ~ dSp ~ "frags"

      private val trp = tpp.map { case (n, p, f) => TeamScore(n, p, f) }

      def unapply(input: String): Option[TeamScore] = {
        val r = trp.parse(input)
        PartialFunction.condOpt(r) {
          case Parsed.Success(x, _) => x
        }
      }
    }

  }

  case class GenericTeamScore(name: String, players: Int, flags: Option[Int], frags: Int)

  trait CreatesGenericTeamScore {
    def project: GenericTeamScore
  }

  case class GenericIndividualScore(name: String, team: String, flag: Option[Int], score: Option[Int], frag: Int, death: Int, host: Option[String], user: Option[String], group: Option[String])

  trait CreatesGenericIndividualScore {
    def project: GenericIndividualScore
  }

  object FlagStyle {

    case class IndividualScore(cn: Int, name: String, team: String, flag: Int, score: Int, frag: Int, death: Int, tk: Int, ping: Int, role: String, host: String, user: Option[String], group: Option[String]) extends CreatesGenericIndividualScore {
      def project = GenericIndividualScore(name, team, Option(flag), Option(score), frag, death, Option(host), user, group)
    }

    object IndividualScore {

      private val cap = " ".? ~ dSp ~ strSp ~ strSp ~ dSp ~ dSp ~ dSp ~ dSp ~ dSp ~ dSp ~ strSp ~ UserHost.parser ~ " ".rep
      private val cpp = cap.map { case (a, b, c, d, e, f, g, h, i, j, k) => IndividualScore(a, b, c, d, e, f, g, h, i, j, k.host, k.userO, k.group) }

      def unapply(input: String): Option[IndividualScore] = {
        val q = cpp.parse(input)
        PartialFunction.condOpt(q) {
          case Parsed.Success(res, _) => res
        }
      }
    }

    case class IndividualScoreDisconnected(name: String, team: String, flag: Int, frag: Int, death: Int) extends CreatesGenericIndividualScore {
      override def project: GenericIndividualScore = GenericIndividualScore(
        name, team, Option(flag), None, frag, death, None, None, None) // todo figure out how to get user & group for disconnected
    }

    object IndividualScoreDisconnected {
      private val cpp = " ".rep ~ strSp ~ strSp ~ dSp ~ dSp ~ dsp ~ " ".rep(1) ~ "-" ~ " ".rep(1) ~ "-" ~ " ".rep(1) ~ "disconnected"
      private val cmp = cpp.map { case (a, b, c, d, e) => IndividualScoreDisconnected(a, b, c, d, e) }

      def unapply(input: String): Option[IndividualScoreDisconnected] = {
        val r = cmp.parse(input)
        PartialFunction.condOpt(r) {
          case Parsed.Success(res, _) => res
        }
      }
    }

    case class TeamScore(name: String, players: Int, frags: Int, flags: Int) extends CreatesGenericTeamScore {
      override def project: GenericTeamScore = GenericTeamScore(name, players, Option(flags), frags)
    }

    object TeamScore {

      private val tpp = P("Team") ~ sp ~ CharsWhile(_ != ':').! ~ ":" ~ sp ~ dSp ~
        "players," ~ sp ~ dSp ~ "frags," ~ sp ~ dSp ~ "flags"

      private val trp = tpp.map { case (n, p, f, fl) => TeamScore(n, p, f, fl) }

      def unapply(input: String): Option[TeamScore] = {
        val r = trp.parse(input)
        PartialFunction.condOpt(r) {
          case Parsed.Success(x, _) => x
        }
      }
    }

  }

}
