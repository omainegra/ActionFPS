package providers.games

import com.actionfps.gameparser.enrichers.JsonGame
import com.google.inject.ImplementedBy
import play.api.libs.json.JsValue

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  */
//@ImplementedBy(classOf[ApiGamesProvider]) // this one will always be up to date as it calls from live api
@ImplementedBy(classOf[ApiAllGamesProvider])
//@ImplementedBy(classOf[SingleJournalGamesProvider])
trait GamesProvider {

  def addHook(hook: JsonGame => Unit): Unit = ()

  def games: Future[Map[String, JsonGame]]

  def removeHook(hook: JsonGame => Unit): Unit = ()

}
