package views.player

import java.nio.file.Path
import java.time.format.DateTimeFormatter

import com.actionfps.accumulation.user.FullProfile
import com.actionfps.achievements.AchievementsRepresentation
import com.actionfps.achievements.immutable.CaptureMapCompletion._
import com.actionfps.achievements.immutable.CaptureMaster
import com.actionfps.ladder.parser.Aggregate.RankedStat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Player {

  val PlayerFile = "player.html"

  def progress_val(percent: Int): Either[Int, Int] = {
    if (percent <= 50) Right(Math.round(90 + 3.6 * percent).toInt)
    else Left(Math.round(3.6 * percent - 270).toInt)
  }

  def fillAchievements(target: Element,
                       achievements: AchievementsRepresentation): Unit = {
    val achieved = target.select(".achievement.achieved").first()
    val partial = target.select(".achievement.partial").first()
    val notStarted = target.select(".achievement.notStarted").first()
    val captureMaster = {
      val cm = target.select(".capture-master").first()
      val cp = cm.clone()
      cm.remove()
      cp
    }

    achievements.completedAchievements
      .map { ca =>
        val cl = achieved.clone()
        cl.select("h3").first().text(ca.title)
        cl.select("p").first().text(ca.description)
        cl.select("a")
          .attr("href", s"/game/?id=${ca.at}")
          .select("relative-time")
          .attr("datetime", ca.at)
          .first()
          .text(ca.at)
        ca.captureMaster.foreach { cm =>
          val cmtgt = captureMaster.clone()
          fillCaptureMaster(cmtgt, cm)
          cl.select("header").first().after(cmtgt)
        }
        cl
      }
      .foreach(target.appendChild)

    achievements.partialAchievements
      .map { ca =>
        val cl = partial.clone()
        cl.select("h3").first().text(ca.title)
        cl.select("p").first().text(ca.description)
        val progress = cl.select(".progress-radial").first()
        progress.select(".overlay").first().text(s"${ca.percent}%")
        val style = progress.attr("style")
        progress_val(ca.percent) match {
          case Right(deg) =>
            progress.attr("style",
                          style.replaceAllLiterally("111deg", s"${deg}deg"))
          case Left(deg) =>
            progress.attr(
              "style",
              style
                .replaceAllLiterally("111deg", s"${deg}deg")
                .replaceAllLiterally("background-image", "-ignore")
                .replaceAllLiterally("-left--ignore", "background-image")
            )
        }
        ca.captureMaster.foreach { cm =>
          val cmtgt = captureMaster.clone()
          fillCaptureMaster(cmtgt, cm)
          cl.select("header").first().after(cmtgt)
        }
        cl
      }
      .foreach(target.appendChild)

    achievements.switchNotAchieveds
      .map { ca =>
        val cl = notStarted.clone()
        cl.select("h3").first().text(ca.title)
        cl.select("p").first().text(ca.description)
        cl
      }
      .foreach(target.appendChild)
    achieved.remove()
    partial.remove()
    notStarted.remove()
  }

  def render(playerHtmlPath: Path,
             player: FullProfile,
             rankedStat: Option[RankedStat]): Html = {
    val htmlB = Jsoup.parse(playerHtmlPath.toFile, "UTF-8")
    val doc = htmlB
    doc.select("h1").first().text(player.user.nickname.nickname)
    val signatureHref = s"/player/signature.svg?id=${player.user.id}"
    doc
      .select("#player-signature")
      .attr("href", signatureHref)
      .select("object")
      .attr("data", signatureHref)

    val fullProfile = player
    fullProfile.achievements match {
      case None =>
        doc.select(".basics, .achievements").remove()
      case Some(achievements) =>
        val achContainer = doc.select(".achievements .achievements").first()
        fillAchievements(achContainer, achievements.buildAchievements)

        fullProfile.locationInfo.flatMap(_.countryName) match {
          case None => doc.select(".country").remove()
          case Some(countryName) =>
            doc.select(".country td").first().text(countryName)
            val built = fullProfile.build
            built.favouriteMap match {
              case Some(map) => doc.select(".favourite_map").first().text(map)
              case None =>
                doc.select(".favourite_map").first().previousSibling().remove()
                doc.select(".favourite_map").first().remove()
            }
        }
        doc
          .select(".time-played")
          .first()
          .text(achievements.playerStatistics.timePlayedStr)
        doc
          .select(".flags")
          .first()
          .text(achievements.playerStatistics.flags.toString)
        doc
          .select(".games-played")
          .first()
          .text(achievements.playerStatistics.gamesPlayed.toString)
        doc
          .select(".frags")
          .first()
          .text(achievements.playerStatistics.frags.toString)

        fullProfile.rank match {
          case None =>
            doc.select(".rank").remove()
          case Some(rank) =>
            doc.select(".rank-none").remove()
            doc
              .select(".elo-rank")
              .first()
              .text(rank.rank.getOrElse("").toString)
            doc
              .select(".elo-points")
              .first()
              .text(Math.round(rank.elo).toString)
        }

    }

    rankedStat match {
      case None => doc.select(".ladder").remove()
      case Some(rs) =>
        doc.select(".ladder-rank").first().text(rs.rank.toString)
        doc
          .select(".ladder-points")
          .first()
          .text(rs.userStatistics.points.toString)
        doc
          .select(".ladder-time-played")
          .first()
          .text(rs.userStatistics.timePlayedText)

        doc
          .select(".ladder-flags")
          .first()
          .text(rs.userStatistics.flags.toString)
        doc
          .select(".ladder-frags")
          .first()
          .text(rs.userStatistics.frags.toString)
        doc
          .select(".ladder-gibs")
          .first()
          .text(rs.userStatistics.gibs.toString)
    }

    val rgili = doc.select(".recent-games li")
    fullProfile.recentGames
      .map { game =>
        val target = rgili.first().clone()
        target.select("a").attr("href", s"/game/?id=${game.id}")
        target.select(".mode-map").first().text(s"${game.mode} @ ${game.map}")
        target
          .select("relative-time")
          .attr("datetime", DateTimeFormatter.ISO_INSTANT.format(game.endTime))
          .first()
          .text(DateTimeFormatter.ISO_INSTANT.format(game.endTime))
        if (game.server.contains("aura")) {
          val demoLink =
            s"http://woop.ac:81/find-demo.php?time=${game.id}&map=${game.map}"
          target.select("a.demo-link").attr("href", demoLink)
        } else {
          target.select(".demo-link").remove()
        }
        target
      }
      .foreach(rgili.first().parent().appendChild)
    rgili.remove()

    Html(htmlB.body().html())
  }

  def fillCaptureMaster(doc: Element, captureMaster: CaptureMaster): Unit = {
    val complete = doc.select("tr.complete").first()
    val incomplete = doc.select("tr.incomplete").first()
    captureMaster.all
      .map {
        case a @ Achieved(map) =>
          val mapName = s"ctf @ ${map}"
          val clone = complete.clone()
          clone.select("th").first().text(mapName)
          clone.select(".cla").first().text(s"${a.cla}/${a.cla}")
          clone.select(".rvsf").first().text(s"${a.rvsf}/${a.rvsf}")
          clone
        case a @ Achieving(map, cla, rvsf) =>
          val clone = incomplete.clone()
          val mapName = s"ctf @ ${map}"
          clone.select("th").first().text(mapName)
          clone.select(".cla").first().text(s"$cla/${a.targetPerSide}")
          clone.select(".rvsf").first().text(s"$rvsf/${a.targetPerSide}")
          clone
      }
      .foreach { clone =>
        incomplete.parents().first().appendChild(clone)
      }
    complete.remove()
    incomplete.remove()
  }

}
