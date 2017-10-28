package lib

import akka.agent.Agent
import com.hazelcast.core.HazelcastInstance
import play.api.Logger

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object HazelcastAgentCache {

  /**
    * Cache an Agent's value in Hazelcast.
    * This will help reload maps effectively between Play application reloads.
    */
  def cachedAgent[T](hazelcastInstance: HazelcastInstance)(
      mapName: String,
      keyName: String)(f: => Future[Agent[T]])(
      implicit executionContext: ExecutionContext): Future[Agent[T]] = {
    val theMap = hazelcastInstance.getMap[String, T](mapName)
    async {
      if (theMap.containsKey(keyName)) {

        /** In case class has changed **/
        Try(theMap.get(keyName)) match {
          case Success(good) => Agent(good)
          case Failure(reason) =>
            Logger.error(s"Failed to fetch cached stuff due to $reason", reason)
            val result = await(f)
            theMap.put(keyName, result.get())
            result
        }
      } else {
        val result = await(f)
        theMap.put(keyName, result.get())
        result
      }
    }

  }
}
