package tl

import tl.TournamentEvent._

/**
  * I'm experimenting with minimalist CQRS style here.
  */
sealed trait Tournament {
  def load(tournamentEvent: TournamentEvent): Tournament

  def initiate(slots: Int): Option[List[TournamentEvent]] = None

  def win(clanId: String): Option[List[TournamentEvent]] = None

  def start(): Option[List[TournamentEvent]] = None

  def registerClan(clanId: String): Option[List[TournamentEvent]] = None
}

object Tournament {

  case object Inactive extends Tournament {
    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case TournamentLaunched(slots) => WaitingToStart(slots, List.empty)
      case _ => this
    }

    override def initiate(slots: Int): Option[List[TournamentEvent]] = Some(List(TournamentLaunched(slots)))
  }

  case class WaitingToStart(slots: Int, filled: List[String]) extends Tournament {

    override def start(): Option[List[TournamentEvent]] = {
      if (filled.size > 1) Some(List(TournamentStarted(filled))) else None
    }

    override def registerClan(clanId: String): Option[List[TournamentEvent]] = {
      if (filled.contains(clanId)) None
      else if (filled.size + 1 == slots) Some(List(ClanRegistered(clanId), TournamentReady))
      else Some(List(ClanRegistered(clanId)))
    }

    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case ClanRegistered(clanId) => copy(filled = filled :+ clanId)
      case TournamentStarted(clans) => ActiveTournament(clans)
      case _ => this
    }

  }

  object ActiveTournament {
    def apply(clans: List[String]): ActiveTournament = ActiveTournament()
  }

  case class ActiveTournament() extends Tournament {
    override def win(clanId: String): Option[List[TournamentEvent]] = {
      None
    }

    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case ClanWon(_, _) => this
      case TournamentWon(_) => Inactive
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
