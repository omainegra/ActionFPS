package views.player

import java.time.format.DateTimeFormatter

import com.actionfps.accumulation.FullProfile
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Player {
  def render(player: FullProfile): Html = {
    val htmlB = Jsoup.parse(lib.Soup.wwwLocation.resolve("player.html").toFile, "UTF-8")
    val doc = htmlB
    doc.select("h1").first().text(player.user.nickname.nickname)
    val fullProfile = player
    fullProfile.achievements match {
      case None =>
        doc.select(".basics, .achievements").remove()
      case Some(achievements) =>

        val achContainer = doc.select(".achievements .achievements")
        achContainer.first().children().remove()

        achievements.buildAchievements.completedAchievements.map { achievement =>
          views.html.player.completed_achievement(achievement)
        }.map(_.body).foreach(achContainer.first().append)
        achievements.buildAchievements.partialAchievements.map {
          achievement =>
            views.html.player.progress_achievement(achievement)
        }.map(_.body).foreach(achContainer.first().append)
        achievements.buildAchievements.switchNotAchieveds.map {
          achievement =>
            views.html.player.none_achievement(achievement)
        }.map(_.body).foreach(achContainer.first().append)

        fullProfile.locationInfo.flatMap(_.countryName) match {
          case None => doc.select(".country").remove()
          case Some(countryName) =>
            doc.select(".country td").first().text(countryName)
        }
        doc.select(".time-played").first().text(achievements.playerStatistics.timePlayedStr)
        doc.select(".flags").first().text(achievements.playerStatistics.flags.toString)
        doc.select(".games-played").first().text(achievements.playerStatistics.gamesPlayed.toString)
        doc.select(".frags").first().text(achievements.playerStatistics.frags.toString)
        fullProfile.rank match {
          case None =>
            doc.select(".rank").remove()
          case Some(rank) =>
            doc.select(".rank-none").remove()
            doc.select(".elo-rank").first().text(rank.rank.getOrElse("").toString)
            doc.select(".elo-points").first().text(Math.round(rank.elo).toString)
        }


        fullProfile.playerGameCounts.map { gc =>
          views.html.game_counts(gc.counts)
        }.map(_.body).foreach(doc.select(".basics").first().append)
    }

    val rgili = doc.select(".recent-games li")
    fullProfile.recentGames.map{ game =>
      val target = rgili.first().clone()
      target.select("a").attr("href", s"/game/?id=${game.id}")
      target.select(".mode-map").first().text(s"${game.mode} @ ${game.map}")
      target.select("time").attr("datetime", DateTimeFormatter.ISO_INSTANT.format(game.endTime))
        .first().text(DateTimeFormatter.ISO_INSTANT.format(game.endTime))
      if(game.server.contains("aura")) {
        val demoLink = s"http://woop.ac:81/find-demo.php?time=${game.id}&map=${game.map}"
        target.select("a.demo-link").attr("href", demoLink)
      } else {
        target.select(".demo-link").remove()
      }
      target
    }.foreach(rgili.first().parent().appendChild)
    rgili.remove()

    Html(htmlB.body().html())
  }
}
