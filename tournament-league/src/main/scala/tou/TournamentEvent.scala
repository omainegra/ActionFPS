package tou

import java.time.ZonedDateTime

sealed trait TournamentEvent {
  def tournamentId: String
}

object TournamentEvent {

  case class TournamentInitiated(latestStartTime: ZonedDateTime, tournamentId: String, depth: Int) extends TournamentEvent

  case class TournamentEntered(tournamentId: String, clanID: String) extends TournamentEvent

  case class TournamentStarted(tournamentId: String) extends TournamentEvent

  case class ClanWarRegistered(tournamentId: String, clanWarId: String, winner: Option[String]) extends TournamentEvent

  case class TournamentCancelled(tournamentId: String) extends TournamentEvent

}
