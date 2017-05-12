package views

import com.actionfps.stats.Clanstats
import lib.WebTemplateRender
import org.jsoup.Jsoup
import play.twirl.api.Html;

/**
  * Created by me on 17/12/2016.
  */
object ClanRankings {

  private def clanRankingsHtmlPath =
    WebTemplateRender.wwwLocation.resolve("clan_rankings.html")

  def render(clanstats: Clanstats): Html = {
    val doc = Jsoup.parse(clanRankingsHtmlPath.toFile, "UTF-8")
    val tbodyTr = doc.select("tbody tr")
    clanstats.onlyRanked.clans.values.toList
      .sortBy(_.elo)
      .reverse
      .take(10)
      .map { clan =>
        val target = tbodyTr.first().clone()
        target
          .select(".clan-name a")
          .attr("href", s"/clan/?id=${clan.id}")
          .first()
          .text(clan.name.getOrElse("-"))
        target.select(".clan-wars").first().text(s"${clan.wars}")
        target.select(".clan-wins").first().text(s"${clan.wins}")
        target.select(".clan-games").first().text(s"${clan.games}")
        target.select(".clan-score").first().text(s"${clan.score}")
        target
          .select(".clan-rank")
          .first()
          .text(clan.rank.map(_.toString).getOrElse(""))
        val clc = target.select(".clan-last-clanwar").first()
        clan.lastClanwar match {
          case None => clc.remove()
          case Some(clanwarId) =>
            clc
              .select("a")
              .attr("href", s"/clanwar/?id=${clanwarId}")
              .select("relative-time")
              .attr("datetime", clanwarId)
              .first()
              .text(s"${clanwarId}")
        }
        target
      }
      .foreach(tbodyTr.first().parent().appendChild)
    tbodyTr.remove()
    Html(doc.body().html())
  }
}
