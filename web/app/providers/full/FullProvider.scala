package providers.full

import java.time.YearMonth

import akka.NotUsed
import akka.agent.Agent
import akka.stream.scaladsl.Source
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.accumulation.achievements.HallOfFame
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.achievements.GameUserEvent
import com.actionfps.clans.{Clanwars, CompleteClanwar}
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.players.PlayersStats
import com.actionfps.stats.Clanstats
import controllers.ProvidesGames
import lib.ClanDataProvider

import scala.concurrent.{ExecutionContext, Future}

abstract class FullProvider()(implicit executionContext: ExecutionContext)
    extends ClanDataProvider
    with ProvidesGames {

  protected[providers] def accumulatorFutureAgent
    : Future[Agent[GameAxisAccumulator]]

  def getRecent(n: Int): Future[List[JsonGame]] =
    accumulatorFutureAgent.map(_.get().recentGames(n))

  def events: Future[List[GameUserEvent]] = {
    accumulatorFutureAgent.map(_.get().events)
  }

  def clanwars: Future[Clanwars] = {
    accumulatorFutureAgent.map(_.get().clanwars)
  }

  def newClanwars: Source[CompleteClanwar, Future[NotUsed]]

  def newGames: Source[JsonGame, Future[NotUsed]]

  def playerRanks: Future[PlayersStats] = {
    accumulatorFutureAgent.map(_.get().shiftedPlayersStats)
  }

  def playerRanksOverTime: Future[Map[YearMonth, PlayersStats]] = {
    accumulatorFutureAgent.map(_.get().playersStatsOverTime)
  }

  def clanstats: Future[Clanstats] = {
    accumulatorFutureAgent.map(_.get().clanstats)
  }

  def hof: Future[HallOfFame] = {
    accumulatorFutureAgent.map(_.get().hof)
  }

  def allGames: Future[List[JsonGame]] = {
    accumulatorFutureAgent.map(_.get().games.values.toList.sortBy(_.id))
  }

  def game(id: String): Future[Option[JsonGame]] = {
    accumulatorFutureAgent.map(_.get().games.get(id))
  }

  def getPlayerProfileFor(id: String): Future[Option[FullProfile]] = {
    accumulatorFutureAgent.map(_.get()).map(_.getProfileFor(id))
  }

}
