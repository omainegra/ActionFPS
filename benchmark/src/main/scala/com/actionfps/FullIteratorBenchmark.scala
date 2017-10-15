package com.actionfps

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionfps.accumulation.{Clan, GameAxisAccumulator}
import com.actionfps.api.Game
import com.actionfps.user.User
import com.typesafe.config.ConfigFactory
import lib.GamesFromSource
import net.sf.ehcache.CacheManager
import org.openjdk.jmh.annotations._
import play.api.Configuration
import play.api.cache.EhCacheApi
import play.api.libs.ws.ahc.AhcWSClient
import providers.ReferenceProvider

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by william on 13/5/17.
  */
@State(Scope.Benchmark)
class FullIteratorBenchmark {

  @Setup()
  def setup(): Unit = {
    val (clans, users) = FullIteratorBenchmark.fetchClansAndUsers()
    initial = GameAxisAccumulator.emptyWithUsers(
      users = users.map(u => u.id -> u).toMap,
      clans = clans.map(c => c.id -> c).toMap
    )
    allGames = FullIteratorBenchmark.fetchGames()
  }

  var allGames: Map[String, Game] = _

  var initial: GameAxisAccumulator = _

  @Benchmark
  def benchAccumulator(): Unit = {
    initial.includeGames(allGames.valuesIterator.toList.sortBy(_.id))
  }

}

object FullIteratorBenchmark {

  def fetchClansAndUsers(): (List[Clan], List[User]) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val wsClient: AhcWSClient = AhcWSClient()
    val cacheManager = CacheManager.create
    try {
      cacheManager.addCache("testCache")
      val cache = cacheManager.getCache("testCache")
      val reference = new ReferenceProvider(
        Configuration.apply(ConfigFactory.load()),
        new EhCacheApi(cache))
      import concurrent.duration._

      val clansF = reference.clans
      val usersF = reference.users

      Await.result(clansF, 5.seconds) -> Await.result(usersF, 5.seconds)
    } finally {
      cacheManager.shutdown()
      wsClient.close()
      Await.result(actorSystem.terminate(), Duration.Inf)
    }
  }

  def fetchGames(): Map[String, Game] = {
    import com.actionfps.formats.json.Formats._
    val gameJournalPath = Paths.get("../journals/games.tsv")
    GamesFromSource
      .loadUnfiltered(scala.io.Source.fromFile(gameJournalPath.toFile))
      .map(game => game.id -> game)
      .toMap
  }

//  println(fetchClansAndUsers())

}
