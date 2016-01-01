package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.{FileInputStream, File}
import javax.inject._

import acleague.ProcessJournalApp
import play.api.{Logger, Configuration}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.Future

/**
  * Do one run of the journal to load the games in.
  *
  */
@Singleton
class SingleJournalGamesProvider @Inject()(configuration: Configuration) extends GamesProvider {

  val sf = new File("journals/journal.log")
  val st = System.currentTimeMillis()
  Logger.info(s"Loading games from journal at ${sf}")

  val games = ProcessJournalApp.parseSource(new FileInputStream(sf)).map(_.cg).map(g => g.id -> g).toMap

  Logger.info(s"Games loaded from journal at ${sf}; ${System.currentTimeMillis() - st}")

  override def getGame(id: String): Future[Option[JsValue]] = Future.successful(games.get(id).map(_.toJson))

  override def getRecent: Future[JsValue] = Future.successful(JsArray(games.toList.sortBy(_._1).takeRight(50).reverse.map(_._2).map(_.toJson)))

}
