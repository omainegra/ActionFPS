package com.actionfps.clans

import com.actionfps.api.Game

/**
  * Created by William on 02/01/2016.
  */
object Clanwars {
  def empty = Clanwars(
    incomplete = Set.empty,
    complete = Set.empty
  )
}

case class Clanwars(incomplete: Set[IncompleteClanwar],
                    complete: Set[CompleteClanwar]) {
  def isEmpty: Boolean = incomplete.isEmpty && complete.isEmpty

  private def includeGame(
      jsonGame: Game): Option[(Clanwars, Option[CompleteClanwar])] = {
    incomplete
      .flatMap(ic => ic.potentialNextGame(jsonGame).map(n => ic -> n))
      .headOption
      .map {
        case (ic, Left(nc)) =>
          copy(incomplete = incomplete - ic + nc) -> None
        case (ic, Right(cc)) =>
          copy(incomplete = incomplete - ic, complete = complete + cc) -> Some(
            cc)
      } orElse {
      Clanwar
        .begin(jsonGame)
        .map(cw => copy(incomplete = incomplete + cw) -> None)
    }
  }

  /**
    * New state, + a complete clanwar if possible
    */
  def includeFlowing(jsonGame: Game): (Clanwars, Option[CompleteClanwar]) =
    includeGame(jsonGame).getOrElse(this -> None)

  def all: Set[Clanwar] = incomplete ++ complete
}
