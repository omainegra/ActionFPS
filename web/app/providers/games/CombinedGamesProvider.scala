package providers.games

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.JsonGame

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 15/01/2017.
  *
  * @usecase Combine all the different game providers into one.
  */
@Singleton
class CombinedGamesProvider @Inject()(journalGamesProvider: JournalGamesProvider,
                                      batchFilesGamesProvider: BatchFilesGamesProvider,
                                      batchURLGamesProvider: BatchURLGamesProvider,
                                      journalTailGamesProvider: JournalTailGamesProvider)
                                     (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  override def games: Future[Map[String, JsonGame]] = {
    for {
      batch <- batchFilesGamesProvider.games
      journal <- journalGamesProvider.games
      batchUrl <- batchURLGamesProvider.games
      jt <- journalTailGamesProvider.games
    } yield batch ++ journal ++ batchUrl
  }

  override def addHook(hook: (JsonGame) => Unit): Unit = {
    batchFilesGamesProvider.addHook(hook)
    journalGamesProvider.addHook(hook)
    batchURLGamesProvider.addHook(hook)
    journalTailGamesProvider.addHook(hook)
    super.addHook(hook)
  }

  override def removeHook(hook: (JsonGame) => Unit): Unit = {
    batchFilesGamesProvider.removeHook(hook)
    journalGamesProvider.removeHook(hook)
    batchURLGamesProvider.removeHook(hook)
    journalTailGamesProvider.removeHook(hook)
    super.removeHook(hook)
  }
}
