package views.clanwar

import lib.Clanner
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 16/12/2016.
  */
object Clanwar {
  def render(clanwar: com.actionfps.clans.ClanwarMeta, showPlayers: Boolean)(implicit clanner: Clanner): Html = {
    val htmlA = views.html.clanwar.render_clanwar(clanwar, showPlayers)
    val htmlB = Jsoup.parse(htmlA.body)

    // Header
    if ( clanwar.completed ) {
      htmlB.select(".lcw").remove()
    }
    val headerA = htmlB.select("header > h2 > a > time").first()
    headerA.text(clanwar.id)
    headerA.attr("datetime", clanwar.id)
    headerA.parent().attr("href", s"/clanwar/?id=${clanwar.id}")

    Html(htmlB.select("body").html())

//    Html(html.select("body").html())
  }
}
