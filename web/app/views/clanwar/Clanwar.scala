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

    def renderHeader(): Unit = {
      // Header
      if (clanwar.completed) {
        htmlB.select(".lcw").remove()
      }
      val headerA = htmlB.select("header > h2 > a > time").first()
      headerA.text(clanwar.id)
      headerA.attr("datetime", clanwar.id)
      headerA.parent().attr("href", s"/clanwar/?id=${clanwar.id}")
    }

    def renderAchievements(): Unit = {
      val ach = htmlB.select(".g-achievements").first()
      (clanwar.achievements, showPlayers) match {
        case (Some(achievements), false) if achievements.nonEmpty =>
          val originalChildren = ach.children()
          achievements.map { achievement =>
            val our = originalChildren.clone()
            our.select("a").attr("href", s"/player/?id=${achievement.user}")
            our.select("a").first().text(achievement.text)
            our
          }.foreach { els =>
            import collection.JavaConverters._
            els.asScala.foreach(ach.appendChild)
          }
          originalChildren.remove()
        case _ =>
          ach.remove()
      }
    }

    renderHeader()
    renderAchievements()

    Html(htmlB.select("body").html())

    //    Html(html.select("body").html())
  }
}
