package views

import com.actionfps.stats.Clanstats
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object ClanRankings {
  def render(clanstats: Clanstats): Html = {
    val doc = Jsoup.parse(lib.Soup.wwwLocation.resolve("clan_rankings.html").toFile, "UTF-8")
    val tbodyTr = doc.select("tbody tr")
    clanstats.onlyRanked.clans.values.toList.sortBy(_.wars).reverse.take(10).map { clan =>
      val target = tbodyTr.first().clone()
      target.select(".clan-name a").attr("href", s"/clan/?id=${clan.id}").first().text(clan.name.getOrElse("-"))
      target.select(".clan-wars").first().text(s"${clan.wars}")
      target.select(".clan-wins").first().text(s"${clan.wins}")
      target.select(".clan-games").first().text(s"${clan.games}")
      target.select(".clan-score").first().text(s"${clan.score}")
      target.select(".clan-rank").first().text(clan.rank.map(_.toString).getOrElse(""))
      target
    }.foreach(tbodyTr.first().parent().appendChild)
    tbodyTr.remove()
    Html(doc.body().html())
  }
}
