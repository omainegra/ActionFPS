package views.clanwar

import lib.Clanner
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 16/12/2016.
  */
object Clanwar {
  def render(clanwar: com.actionfps.clans.ClanwarMeta, showPlayers: Boolean, showGames: Boolean)(implicit clanner: Clanner): Html = {
    val html = Jsoup.parse(lib.Soup.wwwLocation.resolve("clanwar.html").toFile, "UTF-8")
    val clanwarUrl = s"/clanwar/?id=${clanwar.id}"
    if (clanwar.completed) {
      html.select(".incomplete-clanwar").remove()
    }
    html.select(".w header h2 a").attr("href", clanwarUrl)
    html.select(".w header h2 a time").attr("datetime", clanwar.id)
    html.select(".w header h2 a time").first().text(clanwar.id)
    if (!showPlayers) {
      html.select(".g-achievements").remove()
    } else {
      val achs = html.select(".g-achievements").first()
      clanwar.achievements match {
        case None => achs.remove()
        case Some(ach) =>
          val initialChildren = achs.children()
          ach.foreach { achievement =>
            val someChildren = initialChildren.clone()
            someChildren.select("a").attr("href", s"/player/?id=${achievement.user}")
            someChildren.select("a").first().text(achievement.text)
            import collection.JavaConverters._
            someChildren.asScala.foreach(achs.appendChild)
          }
          initialChildren.remove()
      }
    }

    val foundTeams = clanwar.conclusion.teams.flatMap(team => clanner.get(team.clan).map(c => team -> c)).zipWithIndex
    if (foundTeams.length < 2) {
      html.select(".team").remove()
    } else {
      val liNoLink = html.select("li[data-template-id='player-no-link']").first().clone()
      val liLink = html.select("li[data-template-id='player-with-link']").first().clone()
      foundTeams.foreach { case ((team, clan), index) =>
        val teamHtml = html.select(".team").get(index)
        val clanUrl = s"/clan/?id=${team.clan}"
        val header = teamHtml.select(".team-header")
        header.select("h3 a").attr("href", clanUrl)
        header.select("h3 a").first().html(views.html.clan_logo(clan).body)
        header.select(".result .clan a").attr("href", clanUrl)
        header.select(".result .clan a").first().text(team.name.getOrElse(team.clan))
        header.select(".result .score").first().text(s"${team.score}")
        val playersHtml = teamHtml.select(".players").first()
        if (!showPlayers) {
          playersHtml.remove()
        } else {
          playersHtml.select("ol").first().children().remove()
          team.players.values.toList.sortBy(_.flags).reverse.map { player =>
            val clonedThing = player.user match {
              case Some(user) =>
                val mc = liLink.clone()
                mc.select(".name a").first().text(player.name)
                mc.select(".name a").attr("href", s"/player/?id=$user")
                mc
              case None =>
                val mc = liNoLink.clone()
                mc.select(".name").first().text(player.name)
                mc
            }

            if (!player.awards.contains("mvp")) {
              clonedThing.select("img").remove()
            }
            clonedThing.select(".flags").first().text(s"${player.flags}")
            clonedThing.select(".frags").first().text(s"${player.frags}")
            clonedThing
          }.foreach(playersHtml.appendChild)
        }
      }
    }
    Html(html.select("body").html())
  }
}
