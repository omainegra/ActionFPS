package tou

import java.time.ZonedDateTime

import tou.TournamentEvent._

trait TournamentCommand {
  def initiate(tournamentID: String, latestStartTime: ZonedDateTime, depth: Int): Either[String, TournamentInitiated]

  def enterTournament(tournamentID: String, userID: String, clanID: String): Either[String, TournamentEntered]

  def startTournament(tournamentID: String): Either[String, TournamentStarted]

  def registerPlayedClanWar(clanWarId: String, winner: Option[String]): Either[String, ClanWarRegistered]

  def cancelTournament(tournamentID: String): Either[String, TournamentCancelled]
}

case class TournImpl() extends TournamentCommand {
  override def initiate(tournamentID: String, latestStartTime: ZonedDateTime, depth: Int): Either[String, TournamentInitiated] = ???

  override def enterTournament(tournamentID: String, userID: String, clanID: String): Either[String, TournamentEntered] = ???

  override def startTournament(tournamentID: String): Either[String, TournamentStarted] = ???

  override def registerPlayedClanWar(clanWarId: String, winner: Option[String]): Either[String, ClanWarRegistered] = ???

  override def cancelTournament(tournamentID: String): Either[String, TournamentCancelled] = ???
}

object TournImpl {
  val empty: TournImpl = TournImpl()
  implicit val acceptsEvent: AcceptsEvent[TournImpl] = {
    (t: TournImpl, tournamentEvent: TournamentEvent) => ???
  }
}

trait AcceptsEvent[T] {
  def accept(t: T, tournamentEvent: TournamentEvent): T
}

object AcceptsEvent {

  implicit class RichAccept[T](t: T)(implicit acceptsEvent: AcceptsEvent[T]) {
    def accept(tournamentEvent: TournamentEvent): T = acceptsEvent.accept(t, tournamentEvent)

    def iterate(f: T => Either[String, TournamentEvent]): Either[String, T] = {
      f(t).map(accept)
    }
  }

}
