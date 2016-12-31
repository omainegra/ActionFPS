package services

import javax.inject.{Inject, Singleton}

import akka.agent.Agent
import play.api.Configuration
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import play.api.inject.ApplicationLifecycle
import providers.games.GamesProvider
import tl.{ForChallongeApi, OpenMatchPlayers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * Created by me on 31/12/2016.
  */
@Singleton
class ChallongeService @Inject()(wSClient: WSClient, configuration: Configuration, gamesProvider: GamesProvider,
                                 applicationLifecycle: ApplicationLifecycle)(implicit executionContext: ExecutionContext) {

  private val activeTournaments = Agent(Set.empty[String])

  private val forApi = ForChallongeApi(configuration.underlying.getString("challonge.api"))
  private val username = configuration.underlying.getString("challonge.username")
  private val password = configuration.underlying.getString("challonge.password")

  import forApi._
  import scala.async.Async._

  private implicit class RichRequest(wSRequest: WSRequest) {
    def challongeAuth: WSRequest = wSRequest.withAuth(username, password, WSAuthScheme.BASIC)
  }

  def receiveClanwar(winnerClanId: String, loserClanId: String): Future[List[Int]] = {
    // must have fetched some tournaments already!
    firstFetch.flatMap { _ =>
      Future.sequence {
        activeTournaments.get().toList.map { tournamentId =>
          async {
            val ft = ForTournament(tournamentId)
            val response = await(wSClient.url(ft.getTournamentUrl).withQueryString(ft.getTournamentParams: _*).challongeAuth.get())
            val players = ResponseUtil.tryWithInfo(response)(r => OpenMatchPlayers.fromJsonString(r.body))
            players.find(m => Set(m.firstName, m.secondName) == Set(winnerClanId, loserClanId)) match {
              case None => None
              case Some(m) =>
                val winnerId = if (m.firstName == winnerClanId) m.firstId else m.secondId
                val fm = ft.ForMatch(m.matchId)

                val fw = fm.ForWinner(winnerId)
                ResponseUtil.tryWithInfo {
                  await(wSClient.url(fm.updateUrl).challongeAuth.withQueryString(fw.winnerParameter, fw.scoresParameter(m.firstId)).put(""))
                } { k => Some(fm.extractUpdateResponse(k.json).get) }
            }
          }
        }
      }.map(_.flatten)
    }
  }

  def fetchTournaments(): Future[Set[String]] = {
    async {
      val resp = await(wSClient.url(GetTournaments.getProgressTournamentsUrl).challongeAuth.get())
      val tournamentIds = ResponseUtil.tryWithInfo(resp)(r => GetTournaments.extractTournamentIds(r.json))
      await(activeTournaments.alter(_ ++ tournamentIds))
    }
  }

  private val firstFetch = fetchTournaments()

  gamesProvider.addAutoRemoveHook(applicationLifecycle) { jsonGame =>
    for {
      winnerClan <- jsonGame.winnerClan.toList
      loserClan <- jsonGame.clangame.flatMap(_.find(_ != winnerClan))
    } receiveClanwar(winnerClanId = winnerClan, loserClanId = loserClan)
  }

}

object ResponseUtil {
  def tryWithInfo[V](t: WSResponse)(f: WSResponse => V): V = {
    try f(t)
    catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed due to input ${t}, ${t.body}: $e", e)
    }
  }
}
