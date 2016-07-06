package af

import com.actionfps.api.{Game, GamePlayer, GameTeam}
import com.actionfps.players.PlayerStat

/**
  * Created by me on 06/07/2016.
  */
package object flat {

  val gameRender = new ShowLine[Game] {
    def renders = List(
      "id" ~> (_.id),
      "startTime" ~> (_.startTime),
      "endTime" ~> (_.endTime),
      "map" ~> (_.map),
      "mode" ~> (_.mode),
      "state" ~> (_.state),
      "server" ~> (_.server),
      "duration" ~> (_.duration),
      "isTie" ~> (_.isTie),
      "winner" ~> (_.winner),
      "isClangame" ~> (_.isClangame),
      "winnerClan" ~> (_.winnerClan)
    )
  }

  val gameTeamRender = new ShowLine[GameTeam] {
    override def renders: List[(String, (GameTeam) => String)] = List(
      "team" ~> (_.name),
      "flags" ~> (_.flags),
      "frags" ~> (_.frags),
      "clan" ~> (_.clan)
    )
  }

  val gamePlayerRender = new ShowLine[GamePlayer] {
    override def renders: List[(String, (GamePlayer) => String)] = List(
      "name" ~> (_.name),
      "score" ~> (_.score),
      "flags" ~> (_.flags),
      "frags" ~> (_.frags),
      "deaths" ~> (_.deaths),
      "user" ~> (_.user),
      "clan" ~> (_.clan),
      "countryCode" ~> (_.countryCode),
      "countryName" ~> (_.countryName),
      "timezone" ~> (_.timezone)
    )
  }

  val trioRender = new ShowLine[(Game, GameTeam, GamePlayer)] {
    override def renders: List[(String, ((Game, GameTeam, GamePlayer)) => String)] = {
      gameRender.renders.map { case (k, f) =>
        s"game $k" ~> (t => f(t._1))
      } ++
        gameTeamRender.renders.map { case (k, f) =>
          s"team $k" ~> (t => f(t._2))
        } ++
        gamePlayerRender.renders.map { case (k, f) =>
          s"player $k" ~> (t => f(t._3))
        }
    }
  }


  val playerStatRender = new ShowLine[PlayerStat] {
    override def renders: List[(String, (PlayerStat) => String)] = List(
      "user" ~> (_.user),
      "name" ~> (_.name),
      "elo" ~> (_.elo),
      "wins" ~> (_.wins),
      "losses" ~> (_.losses),
      "ties" ~> (_.ties),
      "games" ~> (_.games),
      "score" ~> (_.score),
      "flags" ~> (_.flags),
      "frags" ~> (_.frags),
      "deaths" ~> (_.deaths),
      "lastGame" ~> (_.lastGame)
    )
  }


}
