package tou

import java.time.ZonedDateTime

import af.WIP
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import tou.AcceptsEvent.RichAccept

@WIP
class SampleTournamentTest extends FunSuite {

  test("Basic tournament is initiated") {
    val t = TournImpl.empty
    val result = t.initiate(
      tournamentID = "A",
      latestStartTime = ZonedDateTime.now().plusMonths(1),
      depth = 3
    )
    result.isRight shouldBe true
  }

  test("Cannot initiate the same tournament twice") {
    val t = TournImpl.empty
    val result = t.iterate(_.initiate("A", ZonedDateTime.now(), 3))
      .flatMap(_.iterate(_.initiate("A", ZonedDateTime.now().plusHours(2), 3)))
    result.isRight shouldBe false
  }

  test("Cannot initiate in the past") {
    TournImpl.empty
      .iterate(_.initiate("A", ZonedDateTime.now().minusHours(2), 2))
      .isRight shouldBe false
  }

  test("Cannot cancel a non-existent tournament") {
    TournImpl.empty.iterate(_.cancelTournament("abc")).isRight shouldBe false
  }
  test("Cancel a started tournament") {
    TournImpl.empty.iterate(_.initiate("A", ZonedDateTime.now(), 3))
      .flatMap(_.iterate(_.cancelTournament("A")))
      .isRight shouldBe true
  }
  test("Cannot cancel a cancelled tournament") {
    TournImpl.empty.iterate(_.initiate("A", ZonedDateTime.now(), 3))
      .flatMap(_.iterate(_.cancelTournament("A")))
      .flatMap(_.iterate(_.cancelTournament("A")))
      .isRight shouldBe false
  }

  test("Cannot enter a non-existing tournament") {
    TournImpl.empty.iterate(_.enterTournament("2", "3", "4")).isRight shouldBe false
  }
  test("Can enter a tournament") {
    TournImpl.empty.iterate(_.initiate("A", ZonedDateTime.now(), 3))
      .flatMap(_.iterate(_.enterTournament("A", "C", "B"))).isRight shouldBe true
  }
  test("Cannot enter a tournament twice") {
    TournImpl.empty.iterate(_.initiate("A", ZonedDateTime.now(), 3))
      .flatMap(_.iterate(_.enterTournament("A", "C", "B")))
      .flatMap(_.iterate(_.enterTournament("A", "D", "B"))).isRight shouldBe false
  }
}
