package af.inters

import java.nio.file.Path
import java.time.Instant

import akka.NotUsed
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Source}
import com.actionfps.accumulation.ValidServers
import com.actionfps.gameparser.mserver.ExtractMessage
import com.actionfps.inter.{InterOut, IntersIterator, UserMessage}

import scala.async.Async._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
object IntersFlow {

  trait NicknameToUser {
    def userOf(nickname: String): Option[String]
  }
  object NicknameToUser {
    def empty: NicknameToUser = new NicknameToUser {
      override def userOf(nickname: String): Option[String] = None
    }
  }

  def eventsSource(startInstant: Instant,
                   sourcePath: Path,
                   nicknameToUser: () => Future[NicknameToUser])(
      implicit executionContext: ExecutionContext)
    : Source[InterOut, NotUsed] = {
    FileTailSource
      .lines(sourcePath,
             maxLineSize = 4096,
             pollingInterval = 1.second,
             lf = "\n")
      .filter {
        case ExtractMessage(zdt, _, _) => zdt.toInstant.isAfter(startInstant)
      }
      .via(lineToEventFlow(nicknameToUser, () => Instant.now()))
  }

  private val TimeLeeway = java.time.Duration.ofMinutes(3)

  case class UserMessageFromLine(nicknameToUser: NicknameToUser)(
      implicit validServers: ValidServers) {
    private val regex = """^\[([^\]]+)\] ([^ ]+) says: '(.+)'$""".r

    def unapply(line: String): Option[UserMessage] = {
      PartialFunction.condOpt(line) {
        case ExtractMessage(zdt,
                            validServers.FromLog(validServer),
                            regex(ip, nickname, message @ "!inter"))
            if nicknameToUser.userOf(nickname).isDefined =>
          UserMessage(
            instant = zdt.toInstant,
            serverId = validServer.logId,
            ip = ip,
            nickname = nickname,
            userId = nicknameToUser.userOf(nickname).get,
            messageText = message
          )
      }
    }
  }

  def lineToEventFlow(usersProvider: () => Future[NicknameToUser],
                      instant: () => Instant)(
      implicit validServers: ValidServers,
      executionContext: ExecutionContext): Flow[String, InterOut, NotUsed] = {
    Flow[String]
      .scanAsync(IntersIterator.empty) {
        case (a, line) =>
          async {
            UserMessageFromLine(await(usersProvider()))
              .unapply(line)
              .flatMap(_.interOut)
              .map(a.acceptInterOut)
              .getOrElse(a.resetInterOut)
          }
      }
      .mapConcat(_.interOut.toList)
      .filter { msg =>
        /** Give a 3 minute time skew leeway **/
        msg.userMessage.instant.plus(TimeLeeway).isAfter(instant())
      }
  }

}
