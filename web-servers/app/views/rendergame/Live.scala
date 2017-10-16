package views.rendergame

import com.actionfps.servers.ServerRecord
import lib.WebTemplateRender
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 11/12/2016.
  */
object Live {

  def render(game: com.actionfps.pinger.CurrentGameStatus,
             mapMapping: Map[String, String],
             servers: List[ServerRecord]): Html = {
    if (game.mode.isEmpty || game.map.isEmpty) return Html("")

    val serverUrl = {
      val serverA = game.now.server.server
      servers.find(_.address == serverA).map(_.connectUrl).getOrElse {
        s"assaultcube://${serverA}"
      }
    }

    val html = Jsoup.parse(
      WebTemplateRender.wwwLocation.resolve("live.html").toFile,
      "UTF-8")
    html.select(".server-link").attr("href", serverUrl)
    html.select(".server-link").first().text(game.now.server.shortName)
    html.select(".time-remain").first().text {
      game.minRemain match {
        case 1 => "1 minute remains"
        case 0 => "game finished"
        case m => s"$m minutes remain"
      }
    }
    html
      .select("article")
      .attr(
        "style",
        s"background-image: url('${game.map.flatMap(mapMapping.get).getOrElse("")}')")
    (game.mode, game.map) match {
      case (Some(mode), Some(map)) =>
        html.select(".mode_map").first().text(s"${mode} @ ${map}")
      case _ => html.select(".mode_map").first().remove()
    }
    List("rvsf", "cla").foreach { teamName =>
      if (!game.teams.exists(_.name.equalsIgnoreCase(teamName))) {
        html
          .select(s".team.${teamName}")
          .select(".players, .score, .subscore")
          .remove()
      }
      game.teams
        .find(_.name.equalsIgnoreCase(teamName))
        .map(team =>
          team -> html.select(s".team.${team.name.toLowerCase}").first())
        .filter(_ != null)
        .foreach {
          case (team, teamTable) =>
            team.flags match {
              case Some(flags) =>
                teamTable
                  .select(".team-header .score")
                  .first()
                  .text(s"${flags}")
              case None =>
                teamTable.select(".team-header .score").remove()
            }
            teamTable
              .select(".team-header .subscore")
              .first()
              .text(s"${team.frags}")
            val playerElements = teamTable.select(".players .player")
            team.players.foreach { player =>
              val playerClone = playerElements.first().clone()
              val playerScore = playerClone.select(".flags")
              player.flags match {
                case Some(flags) => playerScore.first().text(s"$flags")
                case None =>
                  playerScore.remove()
              }
              playerClone.select(".frags").first().text(s"${player.frags}")
              player.user match {
                case Some(user) =>
                  playerClone
                    .select(".name span")
                    .first()
                    .tagName("a")
                    .attr("href", s"/player/?id=${user}")
                    .text(player.name)
                case None =>
                  playerClone.select(".name span").first().text(player.name)
              }
              playerElements.first().parent().appendChild(playerClone)
            }
            playerElements.remove()
            val specElements = teamTable.select(".players .spectator")
            team.spectators.toList.flatten.foreach { spec =>
              val specClone = specElements.first().clone()
              val specScore = specClone.select(".flags")
              spec.flags match {
                case Some(flags) => specScore.first().text(s"$flags")
                case None =>
                  specScore.remove()
              }
              specClone.select(".frags").first().text(s"${spec.frags}")
              specClone.select(".name").first().text(spec.name)
              specElements.first().parent().appendChild(specClone)
            }
            specElements.remove()

        }

    }
    if (game.teams.nonEmpty) {
      if (!"rvsf".equalsIgnoreCase(
            game.teams.maxBy(t => t.flags.getOrElse(t.frags)).name)) {
        val cla = html.select(".team.cla").first()
        html.select(".team.rvsf").first().before(cla.clone())
        cla.remove()
      }
    } else {
      html.select(".team").remove()
    }
    game.players match {
      case None => html.select(".dm-players").remove()
      case Some(players) =>
        val theLi = html.select(".dm-players li")
        players.foreach { player =>
          val cline = theLi.first().clone()
          player.user match {
            case None =>
              cline.select("span").first().text(player.name)
            case Some(user) =>
              cline
                .select("span")
                .first()
                .html("")
                .appendElement("a")
                .attr("href", s"/player/?id=${user}")
                .text(player.name)
          }

          theLi.first().parent().appendChild(cline)
        }
        theLi.remove()
    }
    game.spectators match {
      case None => html.select(".spectators").remove()
      case Some(specs) =>
        val theLi = html.select(".spectators li")
        specs.foreach { spec =>
          val cline = theLi.first().clone()
          spec.user match {
            case None =>
              cline.select("span").first().text(spec.name)
            case Some(user) =>
              cline
                .select("span")
                .first()
                .html("")
                .appendElement("a")
                .attr("href", s"/player/?id=${user}")
                .text(spec.name)
          }

          theLi.first().parent().appendChild(cline)
        }
        theLi.remove()
    }
    Html(html.select("body").html())
  }

}
