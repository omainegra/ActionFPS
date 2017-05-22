package af.challonge

import com.exsoloscript.challonge.ChallongeApi
import com.exsoloscript.challonge.model.{Match, Participant, Tournament}

import scala.collection.JavaConverters._

/**
  * Created by william on 22/5/17.
  */
case class InTournament(tournamentUrl: String) {

  def participants(implicit challongeApi: ChallongeApi): List[Participant] =
    challongeApi
      .participants()
      .getParticipants(tournamentUrl)
      .sync()
      .asScala
      .toList

  def matches(implicit challongeApi: ChallongeApi): List[Match] =
    challongeApi.matches().getMatches(tournamentUrl).sync().asScala.toList

}

object InTournament {
  def apply(implicit tournament: Tournament): InTournament = {
    InTournament(tournament.url())
  }
}
