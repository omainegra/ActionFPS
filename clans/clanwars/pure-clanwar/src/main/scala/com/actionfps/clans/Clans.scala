package com.actionfps.clans

import java.time.ZonedDateTime

import com.actionfps.api.{Game, GameAchievement}

case class ClanwarPlayer(flags: Int,
                         frags: Int,
                         score: Int,
                         name: String,
                         user: Option[String],
                         awards: Set[String]) {
  def +(other: ClanwarPlayer): ClanwarPlayer =
    copy(
      flags = flags + other.flags,
      frags = frags + other.frags,
      score = score + other.score,
      awards = awards ++ other.awards
    )

  def award(id: String): ClanwarPlayer = copy(awards = awards + id)
}

case class ClanwarTeam(clan: String,
                       name: Option[String],
                       score: Int,
                       flags: Int,
                       frags: Int,
                       deaths: Int,
                       players: Map[String, ClanwarPlayer],
                       won: Set[String]) {
  def +(other: ClanwarTeam): ClanwarTeam = copy(
    score = score + other.score,
    flags = flags + other.flags,
    frags = frags + other.frags,
    players = players ++ other.players.map {
      case (playerName, player) if players.contains(playerName) =>
        (playerName, players(playerName) + player)
      case np => np
    },
    won = won ++ other.won
  )
}

case class Conclusion(teams: List[ClanwarTeam]) {
  def team(clan: String): Option[ClanwarTeam] = teams.find(_.clan == clan)

  def awardMvps: Conclusion = copy(
    teams = teams.map { team =>
      team.players.maxBy { case (_, player) => player.score } match {
        case (name, player) =>
          team.copy(
            players = team.players.updated(name, player.award("mvp"))
          )
      }
    }
  )

  def named(implicit namer: ClanNamer): Conclusion = copy(
    teams = teams.map(team => team.copy(name = namer.clanName(team.clan)))
  )
}

trait ClanNamer {
  def clanName(clanId: String): Option[String]
}

object ClanNamer {
  def apply(f: String => Option[String]): ClanNamer {
    def clanName(id: String): Option[String]
  } = new ClanNamer {
    override def clanName(id: String): Option[String] = f(id)
  }
}
object Conclusion {

  def conclude(games: List[Game]): Conclusion = {
    val allTeams = for {
      game <- games
      team <- game.teams
      clan <- team.clan
    } yield
      ClanwarTeam(
        clan = clan,
        name = None,
        frags = team.frags,
        deaths = team.players.map(_.deaths).sum,
        score = if (game.winnerClan.contains(clan)) 1 else 0,
        won = Set(game.id),
        flags = team.flags.getOrElse(0),
        players = {
          for {
            player <- team.players
          } yield
            player.name -> ClanwarPlayer(
              flags = player.flags.getOrElse(0),
              frags = player.frags,
              score = player.score.getOrElse(0),
              user = player.user,
              name = player.name,
              awards = Set.empty
            )
        }.toMap
      )

    Conclusion(
      teams = allTeams
        .groupBy(_.clan)
        .mapValues(_.reduce(_ + _))
        .values
        .toList
        .sortBy(_.score)
        .reverse
    )
  }
}

case class ClanwarMeta(id: String,
                       conclusion: Conclusion,
                       endTime: ZonedDateTime,
                       completed: Boolean,
                       teamSize: Int,
                       games: List[Game]) {
  def named(implicit namer: ClanNamer): ClanwarMeta = copy(
    conclusion = conclusion.named
  )

  def achievements: Option[List[GameAchievement]] = {
    Option(games.sortBy(_.id).reverse.flatMap(_.achievements).flatten)
      .filter(_.nonEmpty)
  }
}
