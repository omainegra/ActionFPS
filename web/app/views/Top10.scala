package views

import com.actionfps.players.PlayersStats
import com.actionfps.stats.Clanstats
import lib.WebTemplateRender
import org.jsoup.Jsoup
import play.twirl.api.Html
import views.ladder.Table.PlayerNamer

/**
  * Created by william on 10/5/17.
  */
object Top10 {

  private def top10Path =
    WebTemplateRender.wwwLocation.resolve("top10.html")

  def render(clanstats: Clanstats,
             playersStats: PlayersStats,
             aggregate: com.actionfps.ladder.parser.Aggregate)(
      implicit playerNamer: PlayerNamer): Html = {
    val doc = Jsoup.parse(top10Path.toFile, "UTF-8")
    val tbodyTr = doc.select("tbody tr")
    val interPlayers =
      playersStats.onlyRanked.players.values.toList.sortBy(_.rank).lift
    val ladderPlayers = aggregate.ranked.take(10).lift
    val clans = clanstats.onlyRanked.clans.values.toList
      .sortBy(_.elo)
      .reverse
      .take(10)
      .lift
    (1 to 10)
      .map { n =>
        val target = tbodyTr.first().clone()
        target.select(".rank-place").first().text(s"${n}")
        val ladderPlayer = target.select(".ladder-player").first()
        ladderPlayers(n - 1) match {
          case Some(ladderP) =>
            ladderPlayer
              .select("a")
              .attr("href", s"/player/?id=${ladderP.user}")
              .first()
              .text(playerNamer.nameOf(ladderP.user).getOrElse(ladderP.user))
          case None => ladderPlayer.html("-")
        }

        val interPlayer = target.select(".inter-player").first()
        interPlayers(n - 1) match {
          case Some(interP) =>
            interPlayer
              .select("a")
              .attr("href", s"/player/?id=${interP.user}")
              .first()
              .text(playerNamer.nameOf(interP.user).getOrElse(interP.user))
          case None => interPlayer.html("-")
        }

        val clanTd = target.select(".clan")
        clans(n - 1) match {
          case None => clanTd.html("-")
          case Some(clan) =>
            clanTd
              .select("a")
              .attr("href", s"/clan/?id=${clan.id}")
              .first()
              .text(clan.name.getOrElse(clan.id))
        }
        target
      }
      .foreach(tbodyTr.first().parent().appendChild)
    tbodyTr.remove()
    Html(doc.body().html())
  }
}
