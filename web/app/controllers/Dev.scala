package controllers

import javax.inject.Inject

import com.actionfps.gameparser.Maps
import com.actionfps.pinger._
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

/**
  * Created by me on 11/12/2016.
  */

class Dev @Inject()(common: Common) extends Controller {
  def liveTemplate = Action { implicit req =>
    val mapping = Maps.mapToImage
    val html = views.rendergame.Live.render(mapMapping = mapping, game = Dev.game)
    val fh = Html(html.body + "<hr/>")
    Ok(common.renderTemplate(None, supportsJson = false, None)(fh))
  }
}

object Dev {
  val game = CurrentGameStatus(
    when = "now",
    reasonablyActive = true,
    hasFlags = true,
    map = Some("ac_shine"),
    mode = Some("ctf"),
    minRemain = 5,
    now = CurrentGameNow(
      CurrentGameNowServer(
        server = "aura.woop.us:1234",
        connectName = "aura.woop.us 1239",
        shortName = "aura 123",
        description = "blah bang"
      )
    ),
    updatedTime = "abc",
    players = Some(List("John", "Peter")),
    spectators = Some(List("Smith", "Dave")),
    teams = List(
      CurrentGameTeam(
        name = "rvsf", flags = Some(12), frags = 123,
        spectators = Some(List(CurrentGamePlayer(name = "Speccy", flags = Some(10), frags = 100))),
        players = List(CurrentGamePlayer(name = "peepe", flags = Some(2), frags = 23))
      ),
      CurrentGameTeam(
        name = "cla", flags = Some(13), frags = 114,
        spectators = Some(List(CurrentGamePlayer(name = "Ceppy", flags = None, frags = 99))),
        players = List(CurrentGamePlayer(name = "prepe", flags = None, frags = 29))
      )
    )
  )
}
