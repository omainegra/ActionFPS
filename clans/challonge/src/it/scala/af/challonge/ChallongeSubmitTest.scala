package af.challonge

import af.challonge.ChallongeClient.ClanwarWon
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.exsoloscript.challonge.Challonge
import com.exsoloscript.challonge.model.enumeration.{
  MatchState,
  TournamentType
}
import com.exsoloscript.challonge.model.query.{
  ParticipantQuery,
  TournamentQuery
}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest._
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

  scenario("A tournament is created, started and submitted properly") {

    Given("A tournament is created")

    val tournament = challongeApi
      .tournaments()
      .createTournament(tournamentQuerySample.build())
      .sync()

    info(
      s"Tournament with ID ${tournament.id()} and name ${tournament.name()} was created.")
    info(s"Challonge URL for tournament is: ${tournament.fullChallongeUrl()}")

    And("Participants are added")
    challongeApi
      .participants()
      .bulkAddParticipants(tournament.url(),
                           participantsSample("a", "b", "c", "d").asJava)
      .sync()

    And("Tournament is started")
    challongeApi
      .tournaments()
      .startTournament(tournament.url(), true, true)
      .sync()

    When("I fetch active tournament IDs")
    val tournamentIds =
      Await.result(challongeClient.fetchTournamentIds(), 10.seconds)

    Then("This tournament appears in the set")
    tournamentIds contains tournament.name()

    And("No matches listed are complete")
    val matches =
      challongeApi.matches().getMatches(tournament.url()).sync().asScala
    no(matches.map(_.state())) shouldEqual MatchState.COMPLETE

    When("I submit a match result")
    val submitResult = Await.result(
      challongeClient.attemptSubmit(s"${tournament.id()}",
                                    ClanwarWon("test", "a", 2, "b", 1)),
      Duration.Inf)
    submitResult.value shouldEqual 123

    Then("One match becomes complete")
    val matchesNew =
      challongeApi.matches().getMatches(tournament.url()).sync().asScala
    exactly(1, matchesNew.map(_.state())) shouldEqual MatchState.COMPLETE
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

  private lazy val challongeApi =
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

}
