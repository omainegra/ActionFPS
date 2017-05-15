package views

import com.actionfps.accumulation.achievements.HallOfFame
import lib.WebTemplateRender
import org.jsoup.Jsoup
import play.twirl.api.Html

/**
  * Created by me on 17/12/2016.
  */
object Hof {

  private def hofHtmlPath = WebTemplateRender.wwwLocation.resolve("hof.html")

  def render(hof: HallOfFame): Html = {
    val doc = Jsoup.parse(hofHtmlPath.toFile, "UTF-8")

    val achievement = doc.select(".achievement").first()

    hof.reversed.achievements
      .map { ar =>
        val target = achievement.clone()
        val players = ar.players.map(rp => (rp.user, rp.atGame))
        target.select("h3").first().text(ar.achievement.title)
        target.select("p").first().text(ar.achievement.description)
        val rowTemplate = target.select("tr").first()
        players
          .map {
            case (user, time) =>
              val pt = rowTemplate.clone()
              pt.select("th a")
                .attr("href", s"/player/?id=${user}")
                .first()
                .text(user)
              pt.select("td a").attr("href", s"/game/?id=${time}")
              pt.select("relative-time")
                .attr("datetime", time)
                .first()
                .text(time)
              pt
          }
          .foreach(rowTemplate.parent().appendChild)
        rowTemplate.remove()
        target
      }
      .foreach(achievement.parent().appendChild)
    achievement.remove()

    Html(doc.body().html())
  }
}
