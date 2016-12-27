package controllers

import java.time.{YearMonth, ZoneId}
import javax.inject.Inject

import akka.stream.scaladsl.Source
import com.actionfps.api.Game
import com.actionfps.players.{PlayerStat, PlayersStats}
import play.api.mvc.{Action, AnyContent, Controller}
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

  def gamesPlayersTsv: Action[AnyContent] = Action.async {
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

  def ymPsToLines(ymps: (YearMonth, PlayersStats)): List[String] = {
    ymps match {
      case (ym, ps) =>
        ps.players.values
          .map(pss => af.flat.monthPsRender.renders.map(_._2.apply(ym, pss)).mkString(TAB))
          .toList
    }
  }

  def playerStatTsv: Action[AnyContent] = Action.async {
    val header = af.flat.monthPsRender.renders.map(_._1).mkString(TAB)
    async {
      val pst = await(fullProvider.playerRanksOverTime)
        .toList
        .sortBy(_._1.toString)
        .filterNot { case (ym, x) => ym == YearMonth.now(ZoneId.of("UTC")) }

      val src = Source.single(header) ++ Source(pst).mapConcat(ymPsToLines)
      Ok.chunked(src.map(_ + NEW_LINE)).as(TSV_CONTENT_TYPE)
    }
  }
}
