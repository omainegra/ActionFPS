package providers.games

import acleague.enrichers.JsonGame
import com.google.inject.ImplementedBy
import play.api.libs.json.JsValue

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  */
@ImplementedBy(classOf[ApiGamesProvider]) // this one will always be up to date as it calls from live api
//@ImplementedBy(classOf[ApiAllGamesProvider])
//@ImplementedBy(classOf[SingleJournalGamesProvider])
trait GamesProvider {
  def getGame(id: String): Future[Option[JsValue]]

  def getRecent: Future[JsValue]

  def addHook(jsonGame: JsonGame => Unit): Unit = ()

  def removeHook(jsonGame: JsonGame => Unit): Unit = ()

}
