package views.rendergame

import com.actionfps.api.GameTeam
import com.actionfps.gameparser.enrichers.{JsonGamePlayer, JsonGameTeam}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Render {
  def renderSpectator(target: Element, spectator: JsonGamePlayer): Unit = {
    spectator.flags match {
      case None => target.select(".flags").remove()
      case Some(flags) => target.select(".flags").first().text(s"$flags")
    }
    target.select(".frags").first().text(s"${spectator.frags}")

    spectator.user match {
      case Some(user) => target.select(".name a").attr("href", s"/player/?id=${user}").first().text(spectator.name)
      case None => target.select(".name").first().text(spectator.name)
    }
  }

  def renderPlayer(target: Element, player: JsonGamePlayer): Unit = {
    player.flags match {
      case None => target.select(".flags").remove()
      case Some(flags) => target.select(".flags").first().text(s"$flags")
    }
    target.select(".frags").first().text(s"${player.frags}")
    player.user match {
      case Some(user) => target.select(".name a").attr("href", s"/player/?id=${user}").first().text(player.name)
      case None => target.select(".name").first().text(player.name)
    }
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

    val playersOl = doc.select(".players ol").first()
    val playersPlayers = playersOl.select(".player")
    val playersSpectators = playersOl.select(".spectator")

    team.players.map { player =>
      val target = playersPlayers.first().clone()
      views.rendergame.Render.renderPlayer(target, player)
      target
    }.foreach(playersOl.appendChild)

    teamSpectators.toList.flatten.map { spectator =>
      val target = playersSpectators.first().clone()
      views.rendergame.Render.renderSpectator(target, spectator)
      target
    }.foreach(playersOl.appendChild)

    playersPlayers.remove()
    playersSpectators
      .remove()

    doc.select("div.team").first().addClass(team.name.toLowerCase)

    Html(doc.select("body").html())
  }
}
