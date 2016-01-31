package com.actionfps.clans

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 02/01/2016.
  */
object Clanwars {
  def empty = Clanwars(
    incomplete = Set.empty,
    complete = Set.empty
  )
}

case class Clanwars(incomplete: Set[IncompleteClanwar], complete: Set[CompleteClanwar]) {
  def includeGame(jsonGame: JsonGame): Option[Clanwars] = {
    incomplete.flatMap(ic => ic.potentialNextGame(jsonGame).map(n => ic -> n)).headOption.map {
      case (ic, Left(nc)) =>
        copy(incomplete = incomplete - ic + nc)
      case (ic, Right(cc)) =>
        copy(incomplete = incomplete - ic, complete + cc)
    } orElse Clanwar.begin(jsonGame).map(cw => copy(incomplete = incomplete + cw))
  }

  def includeFlowing(jsonGame: JsonGame) = includeGame(jsonGame).getOrElse(this)

  def all = incomplete ++ complete
}