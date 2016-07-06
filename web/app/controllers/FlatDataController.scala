package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Source
import com.actionfps.api.Game
import com.actionfps.players.PlayerStat
import play.api.mvc.{Action, Controller}
import providers.full.FullProvider

import scala.concurrent.ExecutionContext
import scala.async.Async._

/**
  * Created by me on 06/07/2016.
  */
class FlatDataController @Inject()(fullProvider: FullProvider)
                                  (implicit executionContext: ExecutionContext)
  extends Controller {

  val TSV_CONTENT_TYPE = "text/tab-separated-values"
  val NEW_LINE = "\r\n"

  val TAB = "\t"

  def gameToPlayerLines(g: Game): List[String] =
    g.teams.flatMap(t => t.players.map(p => af.flat.trioRender.renders.map(_._2.apply(g, t, p)).mkString(TAB)))

  def gamesPlayersTsv = Action.async {
    val header = af.flat.trioRender.renders.map(_._1).mkString(TAB)
    async {
      val games = await(fullProvider.allGames)
      val src = Source.single(header) ++ Source(games).mapConcat(gameToPlayerLines)
      Ok.chunked(src.map(_ + NEW_LINE)).as(TSV_CONTENT_TYPE)
    }
  }

  def statToList(g: PlayerStat): String = {
    af.flat.playerStatRender.renders.map(_._2.apply(g)).mkString(TAB)
  }

  def playerStatTsv = Action.async {
    val header = af.flat.playerStatRender.renders.map(_._1).mkString(TAB)
    async {
      val pst = await(fullProvider.playerRanks).players.values
      val src = Source.single(header) ++ Source(pst.toList).map(statToList)
      Ok.chunked(src.map(_ + NEW_LINE)).as(TSV_CONTENT_TYPE)
    }
  }
}
