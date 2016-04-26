package com.actionfps.clans

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.clans.Conclusion.Namer
import play.api.libs.json.{JsObject, Writes, Format, Json}

/**
  * Created by William on 02/01/2016.
  */
object Clanwar {
  def begin(jsonGame: JsonGame): Option[NewClanwar] = {
    jsonGame.clangame.map { clans =>
      NewClanwar(clans = clans, firstGame = jsonGame)
    }
  }

  def gamesAreCompatible(previousGame: JsonGame, nextGame: JsonGame): Boolean = {
    previousGame.server == nextGame.server &&
      previousGame.clangame.nonEmpty && previousGame.clangame == nextGame.clangame &&
      previousGame.teamSize == nextGame.teamSize &&
      previousGame.endTime.plusHours(1).isAfter(nextGame.endTime)
  }


  object ImplicitFormats {


    implicit def clanwarWrites(implicit namer: Namer): Writes[Clanwar] = {
      implicit val ccww = {
        implicit val cpww = Json.format[ClanwarPlayer]
        implicit val ctww = Json.format[ClanwarTeam]
        Writes[Conclusion](con => Json.format[Conclusion].writes(con.named))
      }
      val clanwarFormat: Writes[Clanwar] = Writes[Clanwar] {
        case cc: CompleteClanwar => Json.writes[CompleteClanwar].writes(cc)
        case tw: TwoGamesNoWinnerClanwar => Json.writes[TwoGamesNoWinnerClanwar].writes(tw)
        case nc: NewClanwar => Json.writes[NewClanwar].writes(nc)
      }
      val clanMeta = Json.writes[ClanwarMeta]
      Writes[Clanwar] { cw =>
        clanwarFormat.writes(cw).asInstanceOf[JsObject] ++ clanMeta.writes(cw.meta).asInstanceOf[JsObject]
      }
    }

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

  def allGames = this match {
    case nc: NewClanwar => List(nc.firstGame)
    case tw: TwoGamesNoWinnerClanwar => List(tw.firstGame, tw.secondGame)
    case cc: CompleteClanwar => cc.games
  }

  def conclusion = {
    val conc = Conclusion.conclude(allGames)
    this match {
      case cc: CompleteClanwar => conc.awardMvps
      case _ => conc
    }
  }
}


sealed trait IncompleteClanwar extends Clanwar {
  def potentialNextGame(jsonGame: JsonGame): Option[Either[TwoGamesNoWinnerClanwar, CompleteClanwar]] = this match {
    case nc: NewClanwar =>
      nc.nextGame(jsonGame)
    case tg: TwoGamesNoWinnerClanwar =>
      tg.nextGame(jsonGame).map(Right.apply)
  }
}

case class TwoGamesNoWinnerClanwar(clans: Set[String], firstGame: JsonGame, secondGame: JsonGame) extends IncompleteClanwar {
  def nextGame(jsonGame: JsonGame): Option[CompleteClanwar] = {
    if (Clanwar.gamesAreCompatible(previousGame = secondGame, nextGame = jsonGame)) Option {
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

case class CompleteClanwar(winner: Option[String], clans: Set[String], scores: Map[String, Int], games: List[JsonGame]) extends Clanwar {
  def isTie = winner.isEmpty

  def loser = (clans -- winner.toSet).headOption
}

case class NewClanwar(clans: Set[String], firstGame: JsonGame) extends IncompleteClanwar {
  def nextGame(jsonGame: JsonGame): Option[Either[TwoGamesNoWinnerClanwar, CompleteClanwar]] = {
    if (Clanwar.gamesAreCompatible(previousGame = firstGame, nextGame = jsonGame)) Option {
      val gameScores = GameScores.fromGames(
        clans = clans,
        games = List(firstGame, jsonGame)
      )
      gameScores.winner match {
        case Some(winner) =>
          Right(CompleteClanwar(
            clans = clans,
            games = List(firstGame, jsonGame),
            scores = gameScores.scores,
            winner = Option(winner)
          ))
        case None =>
          Left(TwoGamesNoWinnerClanwar(
            clans = clans,
            firstGame = firstGame,
            secondGame = jsonGame
          ))
      }
    }
    else None
  }
}
