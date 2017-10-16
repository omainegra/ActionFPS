package views.clanwar

import java.nio.file.Path

import com.actionfps.accumulation.Clan
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 16/12/2016.
  */
object Clanwar {

  val ClanwarHtmlFile = "clanwar.html"

  trait ClanIdToClan {
    def clanFrom(clanId: String): Option[Clan]
  }

  object ClanIdToClan {
    def apply(f: String => Option[Clan]): ClanIdToClan = new ClanIdToClan {
      override def clanFrom(clanId: String): Option[Clan] = f(clanId)
    }
  }

  def render(clanwarHtmlPath: Path,
             clanwar: com.actionfps.clans.ClanwarMeta,
             showPlayers: Boolean)(implicit clanner: ClanIdToClan): Html = {

    val htmlB = Jsoup.parse(clanwarHtmlPath.toFile, "UTF-8")

    def renderHeader(): Unit = {
      // Header
      if (clanwar.completed) {
        htmlB.select(".lcw").remove()
      }
      val headerA = htmlB.select("header > h2 > a > local-time").first()
      headerA.text(clanwar.id)
      headerA.attr("datetime", clanwar.id)
      headerA.parent().attr("href", s"/clanwar/?id=${clanwar.id}")
    }

    def renderAchievements(): Unit = {
      val ach = htmlB.select(".g-achievements").first()
      (clanwar.achievements, showPlayers) match {
        case (Some(achievements), false) if achievements.nonEmpty =>
          val originalChildren = ach.children()
          achievements
            .map { achievement =>
              val our = originalChildren.clone()
              our.select("a").attr("href", s"/player/?id=${achievement.user}")
              our.select("a").first().text(achievement.text)
              our
            }
            .foreach { els =>
              import collection.JavaConverters._
              els.asScala.foreach(ach.appendChild)
            }
          originalChildren.remove()
        case _ =>
          ach.remove()
      }
    }

    def renderTeams(): Unit = {
      val teamsEl = htmlB.select(".teams .team")
      val teamHtmlC = teamsEl.first().clone()

      clanwar.conclusion.teams
        .flatMap(team => clanner.clanFrom(team.clan).map(c => team -> c))
        .foreach {
          case (team, clan) =>
            val teamHtml = teamHtmlC.clone()

            def renderTeamHeader(): Unit = {
              teamHtml
                .select(".team-header a")
                .attr("href", s"/clan/?id=${team.clan}")
              teamHtml.select(".team-header h3 a img").attr("src", clan.logo)
              teamHtml
                .select(".team-header .result .clan a")
                .first()
                .text(team.name.getOrElse(team.clan))
              teamHtml
                .select(".team-header .result .score")
                .first()
                .text(s"${team.score}")
            }

            def renderPlayers(): Unit = {
              if (!showPlayers) teamHtml.select(".players").remove()
              else {
                val linkedPlayer =
                  teamHtml.select(".players li.player-linked").first()
                val unlinkedPlayer =
                  teamHtml.select(".players li.player-unlinked").first()
                team.players.values.toList
                  .sortBy(_.flags)
                  .reverse
                  .map { player =>
                    val theLi = player.user match {
                      case Some(user) =>
                        val copied = linkedPlayer.clone()
                        copied
                          .select(".name a")
                          .first()
                          .text(player.name)
                          .attr("href", s"/player/?id=${user}")
                        copied
                      case _ =>
                        val copied = unlinkedPlayer.clone()
                        copied.select(".name span").first().text(player.name)
                        copied
                    }
                    theLi.select(".flags").first().text(s"${player.flags}")
                    theLi.select(".frags").first().text(s"${player.frags}")

                    if (!player.awards.contains("mvp")) {
                      theLi.select("img").remove()
                    }

                    theLi
                  }
                  .foreach(linkedPlayer.parent().appendChild)
                linkedPlayer.remove()
                unlinkedPlayer.remove()
              }
            }

            renderTeamHeader()
            renderPlayers()
            htmlB.select(".teams").first().append(teamHtml.outerHtml())
        }

      teamsEl.remove()
    }

    renderHeader()
    renderAchievements()
    renderTeams()

    Html(htmlB.select("body").html())
  }

}
