package providers.games

import java.io.{File, FileOutputStream, FileInputStream}
import javax.inject.{Inject, Singleton}
import com.actionfps.gameparser.enrichers.JsonGame
import controllers.CommitDescription
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.Success

object CachedJournalGamesProvider {

  sealed trait JournaledCacheState

  case object NoCache extends JournaledCacheState {
    def withGames(games: Map[String, JsonGame]) = LoadedOnlyGames(games)
  }

  case object LoadingCache extends JournaledCacheState {
    def withGames(games: Map[String, JsonGame]) = LoadedOnlyGames(games)

    def withCachedGames(games: Map[String, JsonGame]) = LoadedCache(games)
  }

  case class LoadedCache(games: Map[String, JsonGame]) extends JournaledCacheState {
    def withResults(newGames: Map[String, JsonGame]) =
      LoadedCacheAndResults(cached = games, games = newGames)
  }

  case class LoadedCacheAndResults(cached: Map[String, JsonGame], games: Map[String, JsonGame]) extends JournaledCacheState {
    def diff: List[JsonGame] = (games.keySet -- cached.keySet).toList.map(games)
  }

  case class LoadedOnlyGames(games: Map[String, JsonGame]) extends JournaledCacheState

}

/**
  * Created by William on 2016-04-11.
  */
@Singleton
class CachedJournalGamesProvider @Inject()(configuration: Configuration,
                                           journalGamesProvider: JournalGamesProvider,
                                           applicationLifecycle: ApplicationLifecycle)
                                          (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  Logger.info(s"Using ${getClass.getSimpleName}")

  def targetFile = "cache.tmp"

  lazy val reloadedGames: Future[Map[String, JsonGame]] = {
    addWriteShutdownHook()
    if (CommitDescription.commitDescription.exists(_.contains("#reset-cache"))) {
      Promise[Map[String, JsonGame]]().future
    } else if (!new File(targetFile).exists) {
      Promise[Map[String, JsonGame]]().future
    }
    else {
      Future {
        concurrent.blocking {
          val fis = new FileInputStream(targetFile)
          try {
            val foundGames = Json.fromJson[Map[String, JsonGame]](Json.parse(fis)).get
            Logger.info(s"Found ${foundGames.size} games from cache")
            foundGames
          }
          finally fis.close()
        }
      }
    }
  }

  override def removeHook(hook: (JsonGame) => Unit): Unit = journalGamesProvider.removeHook(hook)

  override def addHook(hook: (JsonGame) => Unit): Unit = journalGamesProvider.addHook(hook)

  /** Rerun hooks if games havebeen updated **/
  lazy val loadNewerGames: Unit = {
    journalGamesProvider.games.onSuccess {
      case newGames if reloadedGames.isCompleted =>
        reloadedGames.value.flatMap(_.toOption).foreach { oldGames =>
          val notSeenGames = (newGames.keySet -- oldGames.keySet).toList.sorted
          if (notSeenGames.isEmpty)
            Logger.info(s"Loaded no unseen games")
          else
            Logger.info(s"Loaded ${notSeenGames.size} unseen games - running hooks on each of them")
          notSeenGames.map(newGames).foreach { game =>
            journalGamesProvider.hooks.get().foreach(_.apply(game))
          }
        }
    }
  }

  override def games: Future[Map[String, JsonGame]] = {
    loadNewerGames
    journalGamesProvider.games.value match {
      case Some(Success(g)) => Future.successful(g)
      case _ => Future.firstCompletedOf(List(journalGamesProvider.games, reloadedGames))
    }
  }

  def addWriteShutdownHook(): Unit = {
    applicationLifecycle.addStopHook(() => Future {
      concurrent.blocking {
        Logger.info(s"Shutting down ${getClass.getSimpleName}: have games ${games.value.flatMap(_.toOption).map(_.size)}")
        games.value.flatMap(_.toOption).foreach { result =>
          Logger.info(s"Saving game cache for ${result.size} games...")
          val fos = new FileOutputStream(targetFile)
          try fos.write(Json.toJson(result).toString().getBytes("UTF-8"))
          finally fos.close()
        }
      }
    })
  }

}
