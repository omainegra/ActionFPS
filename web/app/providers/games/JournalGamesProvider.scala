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
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import providers.games.JournalGamesProvider.NewGameCapture

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}

object JournalGamesProvider {

  def getFileGames(file: File) =
    ProcessJournalApp.parseSource(new FileInputStream(file))
      .map(_.cg)
      .filter(_.validate.isGood)
      .map(g => g.id -> g)
      .toMap

  class NewGameCapture(gameAlreadyExists: String => Boolean, afterGame: Option[JsonGame])(registerGame: JsonGame => Unit) {
    var currentState = MultipleServerParser.empty

    def processLine(line: String) = line match {
      case line@ExtractMessage(date, _, _)
        if afterGame.isEmpty || date.isAfter(afterGame.get.endTime.minusMinutes(20)) =>
        currentState = currentState.process(line)
        PartialFunction.condOpt(currentState) {
          case MultipleServerParserFoundGame(fg, _)
            if !gameAlreadyExists(fg.id) && fg.validate.isGood =>
            registerGame(fg)
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

  override def addHook(jsonGame: (JsonGame) => Unit): Unit = hooks.send(_ + jsonGame)

  override def removeHook(jsonGame: (JsonGame) => Unit): Unit = hooks.send(_ - jsonGame)

  val journalFiles = configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_))

  val gamesA = Future {
    blocking {
      val initialGames = journalFiles.par.map(JournalGamesProvider.getFileGames).reduce(_ ++ _)
      val gamesAgent = Agent(initialGames)
      val lastGame = initialGames.toList.sortBy(_._1).lastOption.map(_._2)
      val ngc = new NewGameCapture(
        gameAlreadyExists = id => gamesAgent.get().contains(id),
        afterGame = lastGame
      )((game) => {
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
