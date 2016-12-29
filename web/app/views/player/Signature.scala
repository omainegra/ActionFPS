package views.player

import java.nio.file.Files

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import play.api.mvc.Result
import play.api.mvc.Results.Ok

/**
  * Created by me on 30/12/2016.
  */
case class Signature(playername: String, interrank: Option[Int], ladderrank: Option[Int], gamecount: Option[Int]) {
  def result: Result = {
    val tplBytes = Files.readAllBytes(lib.Soup.wwwLocation.resolve("sig-template.svg"))
    val tplString = new String(tplBytes, "UTF-8")
    val baseUrl = "https://actionfps.com/"
    val doc = Jsoup.parse(tplString, baseUrl, Parser.xmlParser())
    doc.select("#player-name").first().text(s"${playername}")
    doc.select("#inter-rank").first().text(s"Inter rank: ${interrank.getOrElse("-")}")
    doc.select("#ladder-rank").first().text(s"Ladder rank: ${ladderrank.getOrElse("-")}")
    doc.select("#game-count").first().text(s"Game count: ${gamecount.getOrElse("-")}")
    Ok(doc.outerHtml()).as("image/svg+xml")
  }
}
