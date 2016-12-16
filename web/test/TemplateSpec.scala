import com.actionfps.gameparser.Maps
import com.actionfps.pinger.CurrentGameStatus
import controllers.Dev
import org.jsoup.Jsoup
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by me on 11/12/2016.
  */
class TemplateSpec extends FunSuite with Matchers {
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

  test("Clanwar template does not crash") {
    implicit val namer = Dev.namer
    implicit val clanner = Dev.clanner
    val mapping = Maps.mapToImage
    val html = views.clanwar.Clanwar.render(
      clanwar = Dev.completeClanwar.meta.named,
      showPlayers = true,
      showGames = true
    )
  }
}
