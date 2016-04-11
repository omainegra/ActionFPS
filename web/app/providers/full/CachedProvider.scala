package providers.full

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.JsonGame
import akka.agent.Agent
import com.actionfps.accumulation.FullIterator
import com.hazelcast.client.HazelcastClient
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure, Try}

/**
  * Created by William on 03/01/2016.
  */
@Singleton
class CachedProvider @Inject()(fullProviderR: FullProviderImpl, applicationLifecycle: ApplicationLifecycle,
                               gamesProvider: GamesProvider)
                              (implicit executionContext: ExecutionContext) extends FullProvider() {
  val hz = HazelcastClient.newHazelcastClient()
  val theMap = hz.getMap[String, FullIterator]("stuff")
  private val keyName: String = "fullIterator"
  val logger = Logger(getClass)

  addHook()

  override protected[providers] val fullStuff: Future[Agent[FullIterator]] = async {
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

  def addHook(): Unit = {
    val hook: JsonGame => Unit = (game) => {
      fullStuff.map(_.alter(_.includeGame(game)).foreach(fi => theMap.put(keyName, fi)))
    }
    gamesProvider.addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(gamesProvider.removeHook(hook)))
  }

  applicationLifecycle.addStopHook(() => Future.successful(hz.shutdown()))

  override def reloadReference(): Future[FullIterator] = async {
    val ref = await(fullProviderR.reloadReference())
    await(await(fullStuff).alter(ref))
  }
}
