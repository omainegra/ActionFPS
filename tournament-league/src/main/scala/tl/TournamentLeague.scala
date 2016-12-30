package tl

import tl.TournamentEvent.{ClanRegistered, TournamentReady}

sealed trait Tournament {
  def load(tournamentEvent: TournamentEvent): Tournament
}

object Tournament {

  case object Inactive extends Tournament {
    override def load(tournamentEvent: TournamentEvent): Tournament = this
  }

  case class WaitingToStart(slots: Int, filled: List[String]) extends Tournament {
    def registerClan(clanId: String): Option[List[TournamentEvent]] = {
      if ( filled.contains(clanId) ) None
      else if ( filled.size + 1 == slots ) Some(List(ClanRegistered(clanId), TournamentReady))
      else Some(List(ClanRegistered(clanId)))
    }

    override def load(tournamentEvent: TournamentEvent): Tournament = tournamentEvent match {
      case ClanRegistered(clanId) => copy(filled = filled :+ clanId)
      case _ => this
    }
  }


  def initial: Tournament = Inactive
}

sealed trait TournamentEvent

object TournamentEvent {

  case class ClanRegistered(clanId: String) extends TournamentEvent

  case object TournamentStarted extends TournamentEvent

  case object TournamentReady extends TournamentEvent

}
