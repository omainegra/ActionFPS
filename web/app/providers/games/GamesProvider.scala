package providers.games

import com.actionfps.gameparser.enrichers.JsonGame
import com.google.inject.ImplementedBy
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  *
  * @usecase Provide games in pull and push fashion.
  */
@ImplementedBy(classOf[CombinedGamesProvider])
trait GamesProvider {

  /**
    * Call back when a new game is received.
    */
  def addHook(hook: JsonGame => Unit): Unit = ()

  /**
    * Remove the call back.
    */
  def removeHook(hook: JsonGame => Unit): Unit = ()

  /**
    * Provides a list of currently loaded games.
    * @return Map of game ID to Game
    */
  def games: Future[Map[String, JsonGame]]

  def addAutoRemoveHook(applicationLifecycle: ApplicationLifecycle)(hook: JsonGame => Unit): Unit = {
    addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(removeHook(hook)))
  }

}








