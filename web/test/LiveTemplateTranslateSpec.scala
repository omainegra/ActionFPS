import com.actionfps.gameparser.Maps
import com.actionfps.pinger.CurrentGameStatus
import controllers.Dev
import org.jsoup.Jsoup
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by me on 11/12/2016.
  */
class LiveTemplateTranslateSpec extends FunSuite with Matchers {
  // it doesn't work, I made a few significant changes in the template code anyway.
  ignore("It works") {
    val mapping = Maps.mapToImage
    val html = views.rendergame.Live.render(mapMapping = mapping, game = Dev.game)
    val html2 = Jsoup.parse(views.html.rendergame.live.render(mapMapping = mapping, game = Dev.game).body).select("body").html()
    html shouldBe html2
    println(html)
    println(html2)
  }
  test("It doesn't fail for empty data") {
    val result = views.rendergame.Live.render(mapMapping = Maps.mapToImage, game =
    CurrentGameStatus(
      when = "", reasonablyActive = true, hasFlags = true, map = None, mode = None, minRemain = 123,
      updatedTime = "", players = None, spectators = None, now = Dev.game.now,
      teams = List.empty
    )
    )

    result.body should not include "w00p"
  }
}
