package providers.full

import javax.inject.{Inject, Singleton}

import akka.agent.Agent
import com.actionfps.accumulation.GameAxisAccumulator
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
class HazelcastCachedProvider @Inject()(fullProviderR: FullProviderImpl,
                                        applicationLifecycle: ApplicationLifecycle)
                                       (implicit executionContext: ExecutionContext) extends FullProvider() {
  private val hz = HazelcastClient.newHazelcastClient()
  private val theMap = hz.getMap[String, GameAxisAccumulator]("stuff")
  private val keyName: String = "fullIterator"
  private val logger = Logger(getClass)

  override protected[providers] val fullStuff: Future[Agent[GameAxisAccumulator]] = async {
    if (theMap.containsKey(keyName)) {
      /** In case class has changed **/
      Try(theMap.get(keyName)) match {
        case Success(good) => Agent(good)
        case Failure(reason) =>
          logger.error(s"Failed to fetch cached stuff due to $reason", reason)
          val result = await(fullProviderR.fullStuff)
          theMap.put(keyName, result.get())
          result
      }
    } else {
      val result = await(fullProviderR.fullStuff)
      theMap.put(keyName, result.get())
      result
    }
  }

  applicationLifecycle.addStopHook(() => Future.successful(hz.shutdown()))

  override def reloadReference(): Future[GameAxisAccumulator] = async {
    val ref = await(fullProviderR.reloadReference())
    await(await(fullStuff).alter(ref))
  }
}
