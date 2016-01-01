package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.{File, FileInputStream}
import javax.inject._

import acleague.ProcessJournalApp
import acleague.enrichers.JsonGame
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

  def getFileGames(file: File) =
    ProcessJournalApp.parseSource(new FileInputStream(file))
      .map(_.cg)
      .filter(_.validate.isGood)
      .map(g => g.id -> g)
      .toMap

  val buildInitially = sfs.par.map(getFileGames).reduce(_ ++ _)

  val gamesA = Agent(buildInitially)

  def gamesS = gamesA.get()

  override def games = Future(gamesS)

  Logger.info(s"Games loaded from journals at ${sfs}; ${System.currentTimeMillis() - st}")

  override def getGame(id: String): Future[Option[JsValue]] = Future.successful(gamesS.get(id).map(_.toJson))

  override def getRecent: Future[JsValue] = Future.successful(JsArray(gamesS.toList.sortBy(_._1).takeRight(50).reverse.map(_._2).map(_.toJson)))

  val lastGame = gamesS.toList.sortBy(_._1).lastOption
  var state = MultipleServerParser.empty
  var firstDone = false

  def recentGamesFor(playerId: String): Future[List[JsonGame]] = Future {
    gamesS.valuesIterator.filter(_.teams.exists(_.players.exists(_.user.contains(playerId))))
    .toList.sortBy(_.id).takeRight(7).reverse
  }

  val hooks = Agent(Set.empty[JsonGame => Unit])

  val tailer = new CallbackTailer(sf, false)({
    case line@ExtractMessage(date, _, _) if lastGame.isEmpty || date.isAfter(lastGame.get._2.endTime.minusMinutes(20)) =>
      if (!firstDone) {
        Logger.info(s"Processing again from $line")
        firstDone = true
      }
      state = state.process(line)
      PartialFunction.condOpt(state) {
        case MultipleServerParserFoundGame(fg, _) if !gamesS.contains(fg.id) && fg.validate.isGood =>
          gamesA.send(_ + (fg.id -> fg))
          hooks.get().foreach(f => f(fg))
      }
    case _ =>
  })

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  override def addHook(jsonGame: (JsonGame) => Unit): Unit = {
    hooks.send(_ + jsonGame)
  }

  override def removeHook(jsonGame: (JsonGame) => Unit): Unit = {
    hooks.send(_ - jsonGame)
  }
}
