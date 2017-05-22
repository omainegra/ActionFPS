package af.challonge

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.exsoloscript.challonge.model.Tournament
import com.exsoloscript.challonge.model.enumeration.{
  MatchState,
  TournamentType
}
import com.exsoloscript.challonge.model.query.{
  MatchQuery,
  ParticipantQuery,
  TournamentQuery
}
import com.exsoloscript.challonge.{Challonge, ChallongeApi}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest._
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by me on 31/12/2016.
  *
  * @todo make it tidy
  */
class ChallongeSubmitTest
    extends TestKit(ActorSystem("MySpec"))
    with FeatureSpecLike
    with GivenWhenThen
    with BeforeAndAfterAll {

  ignore("Tournament matches can be extracted") {

    val inTournament = InTournament("af_test_7928130634840821215")
    ForMatchParticipants(matches = inTournament.matches,
                         participants = inTournament.participants)
      .ForPlayers("a", "d")
      .matchingMatch
      .value
      ._1
      .id() shouldEqual 88518405
  }

  scenario("A tournament is created, started and submitted properly") {

    Given("A tournament is created")
    implicit val tournament = createTournament()
    tournamentInfo(tournament)

    And("Participants are added")
    addParticipants("a", "b", "c", "d")

    And("Tournament is started")
    startTournament()

    When("I retrieve the list of matches")
    val it: InTournament = InTournament.apply
    val matches = it.matches

    Then("No matches listed are complete")
    no(matches.map(_.state())) shouldEqual MatchState.COMPLETE

    And("Two matches are OPEN")
    exactly(2, matches.map(_.state())) shouldEqual MatchState.OPEN

    And("One match is PENDING")
    exactly(1, matches.map(_.state())) shouldEqual MatchState.PENDING

    When("I retrieve the list of participants")
    val participants = it.participants

    Then("There is a match between a and d")
    val matchingMatchO = ForMatchParticipants(matches, participants)
      .ForPlayers("a", "d")
      .matchingMatch

    matchingMatchO should not be empty

    When("I set a winner for the match")
    val matchingMatch = matchingMatchO.value
    val matchQuery =
      MatchQuery
        .builder()
        .winnerId(participants.head.id().toString)
        .scoresCsv("2-1")
        .build()

    val updatedMatch = challongeApi
      .matches()
      .updateMatch(tournament.url(), matchingMatch._1.id(), matchQuery)
      .sync()

    info(s"${updatedMatch}")

    Then("One match becomes complete")
    exactly(1, it.matches.map(_.state())) shouldEqual MatchState.COMPLETE
  }

//  "test it works" ignore {
//    val result = Await.result(challongeClient.fetchTournamentIds(), 10.seconds)
//    result shouldBe 5
//  }
//
//  "It works" ignore {
//    val ids = Await.result(challongeClient.fetchTournamentIds(), 5.seconds)
//    val res = Await.result(
//      challongeClient.attemptSubmit("af_test_tournament",
//                                    ClanwarWon("abcd", "woop", 2, "tee", 1)),
//      5.seconds)
//    info(s"$ids")
//    info(s"$res")
//  }
//
//  "It submits an attachment" ignore {
//    val res = Await.result(
//      challongeClient.attemptSubmit("af_test_tournament",
//                                    ClanwarWon("abcd", "imnt", 22, "tyd", 11)),
//      10.seconds)
//    info(s"$res")
//  }

  override def afterAll: Unit = {
    wsClient.close()
    TestKit.shutdownActorSystem(system)
  }
  implicit lazy val mat = ActorMaterializer()

  implicit lazy val wsClient = AhcWSClient()

  private lazy val challongeClient = new ChallongeClient(
    Configuration(ConfigFactory.load()))

  private implicit lazy val challongeApi =
    Challonge.getFor(challongeClient.username, challongeClient.password)

  private def tournamentQuerySample = {
    TournamentQuery
      .builder()
      .name("Test tournament")
      .url(s"af_test_${Math.abs(scala.util.Random.nextLong())}")
      .openSignup(false)
      .tournamentType(TournamentType.SINGLE_ELIMINATION)
  }

  private def participantsSample(ids: String*): List[ParticipantQuery] = {
    ids.map { id =>
      ParticipantQuery.builder().name(id).build()
    }.toList
  }

  def tournamentInfo(implicit tournament: Tournament): Unit = {
    info(
      s"Tournament with ID ${tournament.id()} and name ${tournament.name()} was created.")
    info(s"Challonge URL for tournament is: ${tournament.fullChallongeUrl()}")
  }

  def createTournament(): Tournament = {
    challongeApi
      .tournaments()
      .createTournament(tournamentQuerySample.build())
      .sync()
  }

  def addParticipants(names: String*)(implicit tournament: Tournament): Unit = {
    challongeApi
      .participants()
      .bulkAddParticipants(tournament.url(),
                           participantsSample("a", "b", "c", "d").asJava)
      .sync()
  }

  def startTournament()(implicit tournament: Tournament,
                        challongeApi: ChallongeApi): Unit = {
    challongeApi
      .tournaments()
      .startTournament(tournament.url(), true, true)
      .sync()
  }

}
