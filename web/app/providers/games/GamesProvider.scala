package providers.games

import com.actionfps.gameparser.enrichers.JsonGame
import com.google.inject.ImplementedBy
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  */
//@ImplementedBy(classOf[ApiGamesProvider]) // this one will always be up to date as it calls from live api
@ImplementedBy(classOf[BatchURLGamesProvider])
//@ImplementedBy(classOf[SingleJournalGamesProvider])
trait GamesProvider {

  protected def addHook(hook: JsonGame => Unit): Unit = ()

  def games: Future[Map[String, JsonGame]]

  protected def removeHook(hook: JsonGame => Unit): Unit = ()

  def addAutoRemoveHook(applicationLifecycle: ApplicationLifecycle)(hook: JsonGame => Unit): Unit = {
    addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(removeHook(hook)))
  }

}








