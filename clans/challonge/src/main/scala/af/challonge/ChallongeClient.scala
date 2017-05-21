package af.challonge

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import ChallongeClient._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.async.Async._

@Singleton
class ChallongeClient(uri: String, val username: String, val password: String)(
    implicit wSClient: WSClient,
    executionContext: ExecutionContext) {

  @Inject
  def this(configuration: Configuration)(implicit wSClient: WSClient,
                                         executionContext: ExecutionContext) =
    this(
      ChallongeClient.DefaultUrl,
      configuration.underlying.getString("challonge.username"),
      configuration.underlying.getString("challonge.password")
    )

  private val forApi = ForChallongeApi(uri)

  import forApi._

  private implicit class RichRequest(wSRequest: WSRequest) {
    def challongeAuth: WSRequest =
      wSRequest.withAuth(username, password, WSAuthScheme.BASIC)
  }

  /**
    * Get all the open candidate tournament IDs
    */
  def fetchTournamentIds(): Future[List[String]] = async {
    val resp = await(
      wSClient
        .url(GetTournaments.getProgressTournamentsUrl)
        .challongeAuth
        .get())
    tryWithInfo(resp)(r => GetTournaments.extractTournamentIds(r.json))
  }

  /**
    * Attempt to submit this pair of winners into the tournament.
    * Match ID is returned if addition logically successful.
    */
  def attemptSubmit(tournamentId: String,
                    clanwarWon: ClanwarWon): Future[Option[Int]] = {
    async {
      val forTournament = ForTournament(tournamentId)
      val response = await(
        wSClient
          .url(forTournament.getTournamentUrl)
          .withQueryString(forTournament.getTournamentParams: _*)
          .challongeAuth
          .get())
      val matchPlayers =
        tryWithInfo(response)(r => OpenMatchPlayers.fromJsonString(r.body))
      matchPlayers.find(
        m =>
          Set(m.firstName, m.secondName) == Set(clanwarWon.winnerId,
                                                clanwarWon.loserId)) match {
        case None => None
        case Some(m) =>
          val winnerId =
            if (m.firstName == clanwarWon.winnerId) m.firstId else m.secondId
          val fm = forTournament.ForMatch(m.matchId)
          val fw = fm.ForWinner(winnerId,
                                clanwarWon.winnerScore,
                                clanwarWon.loserScore)
          val endResult = tryWithInfo {
            await(
              wSClient
                .url(fm.updateUrl)
                .challongeAuth
                .withQueryString(fw.winnerParameter,
                                 fw.scoresParameter(m.firstId))
                .put(""))
          } { k =>
            Some(fm.extractUpdateResponse(k.json).get)
          }
          val fc = fm.ForClanwar(clanwarId = clanwarWon.clanwarId)
          await(
            wSClient
              .url(fc.postLinkAttachmentUrl)
              .challongeAuth
              .withQueryString(fc.matchAttachmentParameter,
                               fc.matchDescriptionParameter)
              .post(""))
          endResult
      }
    }
  }

}

object ChallongeClient {

  val DefaultUrl = "https://api.challonge.com/v1"

  case class ClanwarWon(clanwarId: String,
                        winnerId: String,
                        winnerScore: Int,
                        loserId: String,
                        loserScore: Int)

  def tryWithInfo[V](t: WSResponse)(f: WSResponse => V): V = {
    try f(t)
    catch {
      case NonFatal(e) =>
        throw new RuntimeException(s"Failed due to input ${t}, ${t.body}: $e",
                                   e)
    }
  }
}
