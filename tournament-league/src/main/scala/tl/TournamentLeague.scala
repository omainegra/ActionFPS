package tl

import java.time.Instant

import tl.LeagueEvent.{InitTournamentEvent, LeagueTournamentEvent}

sealed trait Tournament {
  def load(tournamentEvent: TournamentEvent): Tournament
}

object Tournament {
  def initial(slots: Int, latestStart: Instant): Tournament = ???
}

case class TournamentState(tournaments: Map[String, Tournament]) {
  me =>

  case class AtTournament(tournamentId: String) {
    def initiate(slots: Int, latestStart: Instant): Option[InitTournamentEvent] = {
      if (tournaments.contains(tournamentId)) None
      else Some(InitTournamentEvent(slots, latestStart))
    }

    def accept(leagueEvent: LeagueEvent): TournamentState = {
      (leagueEvent, tournaments.get(tournamentId)) match {
        case (LeagueTournamentEvent(tournamentEvent), Some(tournament)) =>
          me.copy(tournaments = tournaments.updated(tournamentId, tournament.load(tournamentEvent)))
        case (InitTournamentEvent(slots, latestStart), None) =>
          me.copy(tournaments = tournaments.updated(tournamentId, Tournament.initial(slots, latestStart)))
        case _ => me
      }
    }
  }

}

sealed trait LeagueEvent

object LeagueEvent {

  case class InitTournamentEvent(slots: Int, latestStart: Instant) extends LeagueEvent

  case class LeagueTournamentEvent(tournamentEvent: TournamentEvent) extends LeagueEvent

}

sealed trait TournamentEvent

