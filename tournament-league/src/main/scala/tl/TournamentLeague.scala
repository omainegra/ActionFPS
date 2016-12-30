package tl

import tl.TournamentEvent._

/**
  * I'm experimenting with minimalist CQRS style here.
  */
sealed trait Tournament {
  def load(tournamentEvent: TournamentEvent): Tournament
}

object Tournament {

  case object Inactive extends Tournament {
    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case TournamentLaunched(slots) => WaitingToStart(slots, List.empty)
      case _ => this
    }

    def initiate(slots: Int): TournamentEvent = TournamentLaunched(slots)
  }

  case class WaitingToStart(slots: Int, filled: List[String]) extends Tournament {

    def start(): Option[List[TournamentEvent]] = {
      if (filled.size > 1) Some(List(TournamentStarted(filled))) else None
    }

    def registerClan(clanId: String): Option[List[TournamentEvent]] = {
      if (filled.contains(clanId)) None
      else if (filled.size + 1 == slots) Some(List(ClanRegistered(clanId), TournamentReady))
      else Some(List(ClanRegistered(clanId)))
    }

    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case ClanRegistered(clanId) => copy(filled = filled :+ clanId)
      case TournamentStarted(clans) => ActiveTournament(clans)
      case TournamentReady => this
      case ClanWon(_, _) => this
      case TournamentLaunched(_) => this
      case TournamentWon(_) => this
    }

  }

  object ActiveTournament {
    def apply(clans: List[String]): ActiveTournament = ActiveTournament()
  }

  case class ActiveTournament() extends Tournament {
    def win(clanId: String): Option[List[TournamentEvent]] = {
      None
    }

    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case ClanWon(_, _) => this
      case _ => this
    }
  }

  def initial: Tournament = Inactive

}

sealed trait TournamentEvent

object TournamentEvent {

  case class TournamentLaunched(slots: Int) extends TournamentEvent

  case class ClanRegistered(clanId: String) extends TournamentEvent

  case class TournamentStarted(clans: List[String]) extends TournamentEvent

  case object TournamentReady extends TournamentEvent

  case class ClanWon(clanId: String, clanwarId: Option[String]) extends TournamentEvent

  case class TournamentWon(clanId: String) extends TournamentEvent

}
