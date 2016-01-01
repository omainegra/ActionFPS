package providers.games

import com.google.inject.ImplementedBy
import play.api.libs.json.JsValue
import providers.games.ApiAllGamesProvider

import scala.concurrent.Future

/**
  * Created by William on 01/01/2016.
  */
@ImplementedBy(classOf[ApiGamesProvider])
//@ImplementedBy(classOf[ApiAllGamesProvider])
trait GamesProvider {
  def getGame(id: String): Future[Option[JsValue]]

  def getRecent: Future[JsValue]
}
