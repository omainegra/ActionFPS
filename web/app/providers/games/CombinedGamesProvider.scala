package providers.games

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.JsonGame

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 15/01/2017.
  */
@Singleton
class CombinedGamesProvider @Inject()(journalGamesProvider: JournalGamesProvider,
                                      batchFilesGamesProvider: BatchFilesGamesProvider)
                                     (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  override def games: Future[Map[String, JsonGame]] = {
    for {
      batch <- batchFilesGamesProvider.games
      journal <- journalGamesProvider.games
    } yield batch ++ journal
  }

  override def addHook(hook: (JsonGame) => Unit): Unit = {
    batchFilesGamesProvider.addHook(hook)
    journalGamesProvider.addHook(hook)
    super.addHook(hook)
  }

  override def removeHook(hook: (JsonGame) => Unit): Unit = {
    batchFilesGamesProvider.removeHook(hook)
    journalGamesProvider.removeHook(hook)
    super.removeHook(hook)
  }
}
