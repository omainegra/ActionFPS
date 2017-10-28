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
  private val theMap = hz.getMap[String, GameAxisAccumulator]("stuff")
  private val keyName: String = "fullIterator"
  private val logger = Logger(getClass)

  override protected[providers] val accumulatorFutureAgent
    : Future[Agent[GameAxisAccumulator]] = async {
    if (theMap.containsKey(keyName)) {

      /** In case class has changed **/
      Try(theMap.get(keyName)) match {
        case Success(good) => Agent(good)
        case Failure(reason) =>
          logger.error(s"Failed to fetch cached stuff due to $reason", reason)
          val result = await(fullProviderR.accumulatorFutureAgent)
          theMap.put(keyName, result.get())
          result
      }
    } else {
      val result = await(fullProviderR.accumulatorFutureAgent)
      theMap.put(keyName, result.get())
      result
    }
  }

  override def newClanwars: Source[CompleteClanwar, Future[NotUsed]] =
    fullProviderR.newClanwars

  override def newGames: Source[JsonGame, Future[NotUsed]] =
    fullProviderR.newGames
}
