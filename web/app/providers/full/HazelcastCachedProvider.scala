package providers.full

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.agent.Agent
import akka.stream.scaladsl.Source
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
import com.hazelcast.client.HazelcastClient
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import providers.HazelcastAgentCache
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by William on 03/01/2016.
  *
  * For local development, use Hazelcast.
  * We wish to persist the state of FullIterator between reloads of the app.
  *
  * If we don't, we have performance degradation.
  *
  * So it's only slow on very first load.
  */
@Singleton
class HazelcastCachedProvider @Inject()(fullProviderR: FullProviderImpl)(
    implicit executionContext: ExecutionContext)
    extends FullProvider() {
  private val hz = HazelcastClient.newHazelcastClient()

  override protected[providers] val accumulatorFutureAgent
    : Future[Agent[GameAxisAccumulator]] = HazelcastAgentCache.cachedAgent(hz)(
    mapName = "stuff",
    keyName = "fullIterator")(fullProviderR.accumulatorFutureAgent)

  override def newClanwars: Source[CompleteClanwar, Future[NotUsed]] =
    fullProviderR.newClanwars

  override def newGames: Source[JsonGame, Future[NotUsed]] =
    fullProviderR.newGames
}
