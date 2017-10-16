package tl

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import tl.ChallongeClient._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.async.Async._

@Singleton
class ChallongeClient(
    wSClient: WSClient,
    uri: String,
    username: String,
    password: String)(implicit executionContext: ExecutionContext) {

  @Inject
  def this(wSClient: WSClient, configuration: Configuration)(
      implicit executionContext: ExecutionContext) =
    this(
      wSClient,
      ChallongeClient.DefaultUri,
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
    tryWithInfo("Fetch tournament IDs")(resp)(r =>
      GetTournaments.extractTournamentIds(r.json))
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
        tryWithInfo(s"Fetch match players for ${tournamentId}")(response)(r =>
          OpenMatchPlayers.fromJson(r.json))
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
          val endResult = tryWithInfo(s"Submit for match ${m.matchId}") {
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
  val DefaultUri: String = "https://api.challonge.com/v1"

  case class ClanwarWon(clanwarId: String,
                        winnerId: String,
                        winnerScore: Int,
                        loserId: String,
                        loserScore: Int)

  def tryWithInfo[V](m: String)(t: WSResponse)(f: WSResponse => V): V = {
    try f(t)
    catch {
      case NonFatal(e) =>
        throw new RuntimeException(
          s"Failed due to input ${t} (${m}), ${t.body.take(100)}...: $e",
          e)
    }
  }
}
