package views.rendergame

import com.actionfps.api.GameTeam
import com.actionfps.gameparser.enrichers.{JsonGamePlayer, JsonGameTeam}
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Render {
  def renderSpectator(spectator: JsonGamePlayer): Html = {
    val html = views.html.rendergame.render_game_team_player()
    val js = org.jsoup.Jsoup.parse(html.body)
    val target = js.select(".spectator")
    spectator.flags match {
      case None => target.select(".flags").remove()
      case Some(flags) => target.select(".flags").first().text(s"$flags")
    }
    target.select(".frags").first().text(s"${spectator.frags}")

    spectator.user match {
      case Some(user) => target.select(".name a").attr("href", s"/player/?id=${user}").first().text(spectator.name)
      case None => target.select(".name").first().text(spectator.name)
    }
    Html(target.outerHtml())
  }

  def renderPlayer(player: JsonGamePlayer): Html = {
    val html = views.html.rendergame.render_game_team_player()
    val js = org.jsoup.Jsoup.parse(html.body)
    val target = js.select(".player")
    player.flags match {
      case None => target.select(".flags").remove()
      case Some(flags) => target.select(".flags").first().text(s"$flags")
    }
    target.select(".frags").first().text(s"${player.frags}")
    player.user match {
      case Some(user) => target.select(".name a").attr("href", s"/player/?id=${user}").first().text(player.name)
      case None => target.select(".name").first().text(player.name)
    }
    Html(target.outerHtml())
  }

  def renderTeam(team: GameTeam, mixedGame: MixedGame): Html = {
    val teamSpectators = mixedGame.teamSpectators.get(team.name)
    val html = views.html.rendergame.render_team()
    val doc = Jsoup.parse(html.body)
    if (team.name.equalsIgnoreCase("rvsf")) {
      doc.select(".team-header img").attr("src", doc.select(".team-header img").attr("data-rvsf-src"))
    }
    team.flags match {
      case Some(flags) =>
        doc.select(".team-header .score").first().text(s"$flags")
        doc.select(".team-header .subscore").first().text(s"${team.frags}")
      case None =>
        doc.select(".team-header .score").first().text(s"${team.frags}")
        doc.select(".team-header .subscore").remove()
    }

    team.players.map(views.rendergame.Render.renderPlayer).map(_.body)
      .foreach(doc.select(".players ol").first().append)
    teamSpectators.toList.flatten.map(views.rendergame.Render.renderSpectator)
      .map(_.body).foreach(doc.select(".players ol").first().append)

    doc.select("div.team").first().addClass(team.name.toLowerCase)

    Html(doc.select("body").html())
  }
}
