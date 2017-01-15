package providers.games

import com.actionfps.gameparser.enrichers.JsonGame
import com.google.inject.ImplementedBy
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  */
@ImplementedBy(classOf[CombinedGamesProvider])
trait GamesProvider {

  def addHook(hook: JsonGame => Unit): Unit = ()

  def games: Future[Map[String, JsonGame]]

  def removeHook(hook: JsonGame => Unit): Unit = ()

  def addAutoRemoveHook(applicationLifecycle: ApplicationLifecycle)(hook: JsonGame => Unit): Unit = {
    addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(removeHook(hook)))
  }

}








