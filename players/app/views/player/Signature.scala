package views.player

import java.nio.file.{Files, Path}

import com.actionfps.accumulation.Maps
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import play.api.mvc._
import play.api.mvc.Results._

/**
  * Created by me on 30/12/2016.
  */
case class Signature(playername: String,
                     countrycode: Option[String],
                     interrank: Option[Int],
                     ladderrank: Option[Int],
                     gamecount: Option[Int],
                     map: Option[String]) {

  def result(sigTemplateSvgPath: Path): Result = {
    val tplBytes = Files.readAllBytes(sigTemplateSvgPath)
    val tplString = new String(tplBytes, "UTF-8")
    val baseUrl = "https://actionfps.com/"
    val doc = Jsoup.parse(tplString, baseUrl, Parser.xmlParser())
    doc.outputSettings().indentAmount(0).prettyPrint(false)
    doc.select("#player-name").first().text(s"${playername}")
    doc
      .select("#inter-rank")
      .first()
      .text(s"Inter rank: ${interrank.getOrElse("-")}")
    doc
      .select("#ladder-rank")
      .first()
      .text(s"Ladder rank: ${ladderrank.getOrElse("-")}")
    doc
      .select("#game-count")
      .first()
      .text(s"Game count: ${gamecount.getOrElse("-")}")

    countrycode match {
      case Some(code) =>
        doc
          .select("#country-flag")
          .attr(
            "xlink:href",
            s"https://cdnjs.cloudflare.com/ajax/libs/flag-icon-css/2.8.0/flags/1x1/${code.toLowerCase()}.svg")
      case None =>
        doc.select("#country-flag").remove()
    }

    map.flatMap(Maps.mapToImage.get) match {
      case Some(bgImage) =>
        doc.select("#bgimage").attr("xlink:href", bgImage)
      case None =>
        doc.select("#bgimage").remove()
    }
    Ok(doc.outerHtml()).as("image/svg+xml")
  }
}
object Signature {
  val SigTemplateFilename = "sig-template.svg"
}
