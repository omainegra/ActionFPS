package services

import java.time.ZonedDateTime

import akka.NotUsed
import akka.stream.scaladsl._
import com.actionfps.clans.CompleteClanwar
import play.api.Logger
import tl.ChallongeClient.ClanwarWon
import tl.{ChallongeClient, WinFlow}

import scala.concurrent.ExecutionContext

object ChallongeService {
  def sinkFlow(challongeClient: ChallongeClient)(
      implicit executionContext: ExecutionContext)
    : Flow[CompleteClanwar, Option[Int], NotUsed] = {
    Flow[CompleteClanwar]
      .filter { clanwar =>
        // don't allow old clanwars to be committed
        // todo add a journal for clanwar persistence
        ZonedDateTime
          .parse(clanwar.id)
          .isAfter(ZonedDateTime.now().minusHours(3))
      }
      .map { cc =>
        Logger.info(s"Pushing down clanwar: ${cc}")
        cc
      }
      .mapConcat(cc => WinFlow.detectWinnerLoserClanwar(cc).toList)
      .map { wc =>
        Logger.info(s"Received clanwar: ${wc}")
        wc
      }
      .via(WinFlow(challongeClient).clanwarWon)
      .alsoTo(Sink.foreach(item => Logger.info(s"Sunk clanwar: ${item}")))
  }
}
