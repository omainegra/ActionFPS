package views

import com.actionfps.players.PlayersStats
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object PlayerRanks {
  def render(playersStats: PlayersStats): Html = {
    val htmlB = Jsoup.parse(lib.Soup.wwwLocation.resolve("player_ranks.html").toFile, "UTF-8")

    val trs = htmlB.select("tbody > tr")
    playersStats.onlyRanked.players.values.toList.sortBy(_.rank).map { player =>
      val target = trs.first().clone()
      target.select(".rank").first().text(player.rank.getOrElse("-").toString)
      target.select(".user a").attr("href", s"/player/?id=${player.user}").first().text(player.name)
      target.select(".games").first().text(s"${player.games}")
      target.select(".won").first().text(s"${player.wins}")
      target.select(".elo").first().text(s"${Math.round(player.elo)}")
      target.select(".score").first().text(s"${player.score}")
      target.select(".last-played a").attr("href", s"/player/?id=${player.lastGame}")
      target.select(".last-played time").attr("datetime", player.lastGame).first().text(player.lastGame)
      target
    }.foreach(htmlB.select("tbody").first().appendChild)

    trs.remove()
    Html(htmlB.body().html())
  }
}
