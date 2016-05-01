package views.rendergame

import com.actionfps.gameparser.Maps
import com.actionfps.gameparser.enrichers.{JsonGamePlayer, JsonGame}

case class NowServer(server: String)

case class Now(server: NowServer, minRemain: Option[Int])

case class GDA(text: String, user: String)

object MixedGame {
  def fromJsonGame(jsonGame: JsonGame) = {
    MixedGame(isNew = false, game = jsonGame, now = None, players = None, spectators = None, clanwar = jsonGame.clanwar,
      achievements = None, teamSpectators = Map.empty)
  }
}

case class MixedGame(isNew: Boolean, game: JsonGame, now: Option[Now], players: Option[List[String]]
                     , spectators: Option[List[String]],
                     clanwar: Option[String], achievements: Option[List[GDA]], teamSpectators: Map[String, List[JsonGamePlayer]]) {
  mg =>

  import game._

  def id: Option[String] = Option(game.id).filter(_.nonEmpty)

  def acLink: Option[String] = now.map(now => s"assaultcube://${now.server.server}")

  def onServer = now.map(_.server.server)

  def demoLink = if (now.isEmpty && server.contains("aura"))
    id.map(i => s"""http://woop.ac:81/find-demo.php?time=$i&map=$map""")
  else None

  def url = id.map(i => s"/game/?id=$i")

  def heading = s"$mode @ $map"

  def bgImage = Maps.resource.maps.get(map).map(_.image)

  def bgStyle = bgImage.map(i => s"background-image: url('$i')").getOrElse("")

  def className = s"GameCard game " + (if (now.isDefined) "isLive" else if (isNew) "isNew" else "")
}
