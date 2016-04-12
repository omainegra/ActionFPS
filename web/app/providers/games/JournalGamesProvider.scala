package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.{File, FileInputStream}
import javax.inject._
import com.actionfps.gameparser.ProcessJournalApp
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.gameparser.mserver.{ExtractMessage, MultipleServerParser, MultipleServerParserFoundGame}
import akka.agent.Agent
import com.actionfps.accumulation.{GeoIpLookup, ValidServers}
import lib.CallbackTailer
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import providers.games.JournalGamesProvider.NewGameCapture

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}
import ValidServers.Validator._
import ValidServers.ImplicitValidServers._

object JournalGamesProvider {

  def getFileGames(file: File) = {
    val fis = new FileInputStream(file)
    try ProcessJournalApp.parseSource(fis)
      .map(_.cg)
      .filter(_.validate.isGood)
      .filter(_.validateServer)
      .map(_.flattenPlayers)
      .map(g => g.id -> g)
      .toMap
    finally fis.close()
  }

  class NewGameCapture(gameAlreadyExists: String => Boolean, afterGame: Option[JsonGame])(registerGame: JsonGame => Unit) {
    var currentState = MultipleServerParser.empty

    def processLine(line: String) = line match {
      case line@ExtractMessage(date, _, _)
        if afterGame.isEmpty || date.isAfter(afterGame.get.endTime.minusMinutes(20)) =>
        currentState = currentState.process(line)
        PartialFunction.condOpt(currentState) {
          case MultipleServerParserFoundGame(fg, _)
            if !gameAlreadyExists(fg.id) && fg.validate.isGood && fg.validateServer =>
            registerGame(fg.flattenPlayers)
        }
      case _ =>
    }
  }

}

/**
  * Load in the list of journals - and tail the last one to grab the games.
  */
@Singleton
class JournalGamesProvider @Inject()(configuration: Configuration,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  val hooks = Agent(Set.empty[JsonGame => Unit])

  override def addHook(f: (JsonGame) => Unit): Unit = hooks.send(_ + f)

  override def removeHook(f: (JsonGame) => Unit): Unit = hooks.send(_ - f)

  val journalFiles = configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_))

  implicit private val geoIp = GeoIpLookup

  val gamesA = Future {
    blocking {
      val initialGames = journalFiles
        .par
        .map(JournalGamesProvider.getFileGames)
        .map(_.mapValues(_.withGeo).toMap)
        .reduce(_ ++ _)
      val gamesAgent = Agent(initialGames)
      val lastGame = initialGames.toList.sortBy(_._1).lastOption.map(_._2)
      val ngc = new NewGameCapture(
        gameAlreadyExists = id => gamesAgent.get().contains(id),
        afterGame = lastGame
      )((ggame) => {
        val game = ggame.withGeo
        gamesAgent.send(_ + (game.id -> game))
        hooks.get().foreach(f => f(game))
      })
      val tailer = new CallbackTailer(journalFiles.last, false)(ngc.processLine)
      applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))
      gamesAgent
    }
  }

  override def games: Future[Map[String, JsonGame]] = gamesA.map(_.get())

}
