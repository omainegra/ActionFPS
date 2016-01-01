package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.{File, FileInputStream}
import javax.inject._

import acleague.ProcessJournalApp
import acleague.mserver.{ExtractMessage, MultipleServerParser, MultipleServerParserFoundGame}
import akka.agent.Agent
import lib.CallbackTailer
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, JsValue}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Load in the list of journals - and tail the last one to grab the games.
  */
@Singleton
class JournalGamesProvider @Inject()(configuration: Configuration,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  import collection.JavaConverters._

  val sfs = configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_))
  val sf = sfs.last
  val st = System.currentTimeMillis()
  Logger.info(s"Loading games from journal ats ${sfs}")

  def getFileGames(file: File) = ProcessJournalApp.parseSource(new FileInputStream(file)).map(_.cg).map(g => g.id -> g).toMap

  val gamesA = Agent(sfs.par.map(getFileGames).reduce(_ ++ _))

  def games = gamesA.get()

  Logger.info(s"Games loaded from journals at ${sfs}; ${System.currentTimeMillis() - st}")

  override def getGame(id: String): Future[Option[JsValue]] = Future.successful(games.get(id).map(_.toJson))

  override def getRecent: Future[JsValue] = Future.successful(JsArray(games.toList.sortBy(_._1).takeRight(50).reverse.map(_._2).map(_.toJson)))

  val lastGame = games.toList.sortBy(_._1).lastOption
  var state = MultipleServerParser.empty
  var firstDone = false
  val tailer = new CallbackTailer(sf, false)({
    case line @ ExtractMessage(date, _, _) if lastGame.isEmpty || date.isAfter(lastGame.get._2.endTime.minusMinutes(20)) =>
      if ( !firstDone ) {
        Logger.info(s"Processing again from $line")
        firstDone = true
      }
      state = state.process(line)
      PartialFunction.condOpt(state) {
        case MultipleServerParserFoundGame(fg, _) if !games.contains(fg.id) =>
          gamesA.send(_ + (fg.id -> fg))
      }
    case _ =>
  })

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

}
