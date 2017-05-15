package com.actionfps.clans

import com.actionfps.api.Game

/**
  * Created by William on 02/01/2016.
  */
object Clanwar {
  def begin(jsonGame: Game): Option[NewClanwar] = {
    jsonGame.clangame.map { clans =>
      NewClanwar(clans = clans, firstGame = jsonGame)
    }
  }

  def gamesAreCompatible(previousGame: Game, nextGame: Game): Boolean = {
    previousGame.server == nextGame.server &&
    previousGame.clangame.nonEmpty && previousGame.clangame == nextGame.clangame &&
    previousGame.teamSize == nextGame.teamSize &&
    previousGame.endTime.plusHours(1).isAfter(nextGame.endTime)
  }

}

sealed trait Clanwar {
  def id: String = allGames.head.id

  def clans: Set[String]

  def meta: ClanwarMeta = ClanwarMeta(
    conclusion = conclusion,
    teamSize = allGames.head.teamSize,
    id = id,
    endTime = allGames.last.endTime,
    games = allGames.sortBy(_.id),
    completed = this match {
      case cc: CompleteClanwar => true
      case _ => false
    }
  )

  def allGames: List[Game] = this match {
    case nc: NewClanwar => List(nc.firstGame)
    case tw: TwoGamesNoWinnerClanwar => List(tw.firstGame, tw.secondGame)
    case cc: CompleteClanwar => cc.games
  }

  def conclusion: Conclusion = {
    val conc = Conclusion.conclude(allGames)
    this match {
      case cc: CompleteClanwar => conc.awardMvps
      case _ => conc
    }
  }
}

sealed trait IncompleteClanwar extends Clanwar {
  def potentialNextGame(jsonGame: Game)
    : Option[Either[TwoGamesNoWinnerClanwar, CompleteClanwar]] = this match {
    case nc: NewClanwar =>
      nc.nextGame(jsonGame)
    case tg: TwoGamesNoWinnerClanwar =>
      tg.nextGame(jsonGame).map(Right.apply)
  }
}

case class TwoGamesNoWinnerClanwar(clans: Set[String],
                                   firstGame: Game,
                                   secondGame: Game)
    extends IncompleteClanwar {
  def nextGame(jsonGame: Game): Option[CompleteClanwar] = {
    if (Clanwar.gamesAreCompatible(previousGame = secondGame,
                                   nextGame = jsonGame)) Option {
      val gameScores = GameScores.fromGames(
        clans = clans,
        games = List(firstGame, secondGame, jsonGame)
      )
      CompleteClanwar(
        winner = gameScores.winner,
        scores = gameScores.scores,
        clans = clans,
        games = List(firstGame, secondGame, jsonGame)
      )
    } else None
  }

}

/**
  *
  * @param winner
  * @param clans
  * @param scores map from clan ID to their score
  * @param games
  */
case class CompleteClanwar(winner: Option[String],
                           clans: Set[String],
                           scores: Map[String, Int],
                           games: List[Game])
    extends Clanwar {
  def isTie: Boolean = winner.isEmpty

  def loser: Option[String] = (clans -- winner.toSet).headOption
}

case class NewClanwar(clans: Set[String], firstGame: Game)
    extends IncompleteClanwar {
  def nextGame(jsonGame: Game)
    : Option[Either[TwoGamesNoWinnerClanwar, CompleteClanwar]] = {
    if (Clanwar.gamesAreCompatible(previousGame = firstGame,
                                   nextGame = jsonGame)) Option {
      val gameScores = GameScores.fromGames(
        clans = clans,
        games = List(firstGame, jsonGame)
      )
      gameScores.winner match {
        case Some(winner) =>
          Right(
            CompleteClanwar(
              clans = clans,
              games = List(firstGame, jsonGame),
              scores = gameScores.scores,
              winner = Option(winner)
            ))
        case None =>
          Left(
            TwoGamesNoWinnerClanwar(
              clans = clans,
              firstGame = firstGame,
              secondGame = jsonGame
            ))
      }
    } else None
  }
}
