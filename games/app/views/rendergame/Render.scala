package views.rendergame

import com.actionfps.api.GameTeam
import com.actionfps.gameparser.enrichers.JsonGamePlayer
import lib.WebTemplateRender
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Render {

  private def renderGameHtmlPath =
    WebTemplateRender.wwwLocation.resolve("render_game.html").toFile

  def fillHeader(target: Element, mixedGame: MixedGame): Unit = {
    mixedGame.url.foreach(url => target.select("a").first().attr("href", url))
    target.select(".heading").first().text(mixedGame.heading)
    if (mixedGame.now.isEmpty) {
      target
        .select("relative-time")
        .attr("datetime", mixedGame.game.endTime.toString)
        .first()
        .text(mixedGame.game.endTime.toString)
    } else {
      target.select("relative-time").remove()
    }
    mixedGame.demoLink match {
      case None => target.select(".demo-link").remove()
      case Some(link) =>
        target.select("a.demo-link").attr("href", link)
    }
    mixedGame.acLink match {
      case None => target.select(".server-link").remove()
      case Some(acLink) =>
        target
          .select("a.server-link")
          .attr("href", acLink)
          .first()
          .text(s"on ${mixedGame.now.get.server}")
    }

    mixedGame.now.flatMap(_.minRemain).map {
      case 0 => "game finished"
      case 1 => "1 minute remains"
      case n => s"$n minutes remain"
    } match {
      case None => target.select(".time-remain").remove()
      case Some(mins) => target.select(".time-remain").first().text(mins)
    }
  }

  def renderMixedGame(mixedGame: MixedGame): Html = {
    val doc: Document = Jsoup.parse(renderGameHtmlPath, "UTF-8")
    fillHeader(doc.select("header").first(), mixedGame)
    doc
      .select("article")
      .addClass(mixedGame.className)
      .attr("style", mixedGame.bgStyle)

    mixedGame.players match {
      case None => doc.select(".dm-players").remove()
      case Some(dms) =>
        val lis = doc.select(".dm-players").select("li")
        dms
          .map { dm =>
            val tgt = lis.first().clone()
            tgt.select("span").first().text(dm)
            tgt
          }
          .foreach(lis.first().parent().appendChild)
        lis.remove()
    }
    mixedGame.spectators match {
      case None =>
        doc.select(".spectators").remove()
      case Some(specs) =>
        val lis = doc.select(".spectators li")
        specs
          .map { spec =>
            val tgt = lis.first().clone()
            tgt.select("span").first().text(spec)
            tgt
          }
          .foreach(lis.first().parent().appendChild)
        lis.remove()
    }

    val teams = doc.select(".teams > .team")

    mixedGame.game.teams
      .map { team =>
        val target = teams.first().clone()
        views.rendergame.Render.renderTeam(target, team, mixedGame)
        target
      }
      .foreach(doc.select(".teams").first().appendChild)
    teams.remove()

    mixedGame.game.clanwar match {
      case None => doc.select(".of-clanwar").remove()
      case Some(clanwar) =>
        doc.select(".of-clanwar a").attr("href", s"/clanwar/?id=$clanwar")
        doc
          .select(".of-clanwar relative-time")
          .attr("datetime", clanwar)
          .first()
          .text(clanwar)
    }
    import scala.collection.JavaConverters._
    mixedGame.game.achievements match {
      case None => doc.select(".g-achievements").remove()
      case Some(achievements) =>
        val theChildren = doc.select(".g-achievements").first().children()
        achievements
          .map { ach =>
            val cloned = theChildren.clone()
            cloned
              .select("a")
              .attr("href", s"/player/?id=${ach.user}")
              .first()
              .text(ach.text)
            cloned
          }
          .flatMap(_.asScala.toList)
          .foreach(doc.select(".g-achievements").first().appendChild)
        theChildren.remove()
    }

    Html(doc.select("body").html())
  }

  def renderSpectator(target: Element, spectator: JsonGamePlayer): Unit = {
    spectator.flags match {
      case None => target.select(".flags").remove()
      case Some(flags) => target.select(".flags").first().text(s"$flags")
    }
    target.select(".frags").first().text(s"${spectator.frags}")

    spectator.user match {
      case Some(user) =>
        target
          .select(".name a")
          .attr("href", s"/player/?id=${user}")
          .first()
          .text(spectator.name)
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
      case Some(user) =>
        target
          .select(".name a")
          .attr("href", s"/player/?id=${user}")
          .first()
          .text(player.name)
      case None => target.select(".name").first().text(player.name)
    }
  }

  def renderTeam(doc: Element, team: GameTeam, mixedGame: MixedGame): Unit = {
    val teamSpectators = mixedGame.teamSpectators.get(team.name)
    if (team.name.equalsIgnoreCase("rvsf")) {
      doc
        .select(".team-header img")
        .attr("src", doc.select(".team-header img").attr("data-rvsf-src"))
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

    team.players
      .map { player =>
        val target = playersPlayers.first().clone()
        views.rendergame.Render.renderPlayer(target, player)
        target
      }
      .foreach(playersOl.appendChild)

    teamSpectators.toList.flatten
      .map { spectator =>
        val target = playersSpectators.first().clone()
        views.rendergame.Render.renderSpectator(target, spectator)
        target
      }
      .foreach(playersOl.appendChild)

    playersPlayers.remove()
    playersSpectators
      .remove()

    doc.select("div.team").first().addClass(team.name.toLowerCase)
  }
}
