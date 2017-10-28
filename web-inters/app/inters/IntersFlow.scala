package af.inters

import java.time

import com.actionfps.gameparser.mserver.ExtractMessage
import com.actionfps.inter.{IntersIterator, UserMessage}
import com.actionfps.servers.ValidServers

import scala.async.Async._
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
    def empty: NicknameToUser = Function.const(None)
  }

  val InterMessage = "!inter"

  case class UserMessageFromLine(nicknameToUser: NicknameToUser)(
      implicit validServers: ValidServers) {
    private val regex = """^\[([^\]]+)\] ([^ ]+) says: '(.+)'$""".r

    def unapply(line: String): Option[UserMessage] = {
      if (line.contains(InterMessage))
        PartialFunction.condOpt(line) {
          case ExtractMessage(zdt,
                              validServers.FromLog(validServer),
                              regex(ip, nickname, message @ `InterMessage`))
              if nicknameToUser.userOf(nickname).isDefined =>
            UserMessage(
              instant = zdt.toInstant,
              serverId = validServer.logId,
              ip = ip,
              nickname = nickname,
              userId = nicknameToUser.userOf(nickname).get,
              messageText = message
            )
        } else None
    }
  }

  case class ScanIterators(usersProvider: () => Future[NicknameToUser])(
      implicit validServers: ValidServers) {
    def initial: IntersIterator = IntersIterator.empty

    def scanAsync(intersIterator: IntersIterator, line: String)(
        implicit executionContext: ExecutionContext)
      : Future[IntersIterator] = {
      async {
        UserMessageFromLine(await(usersProvider()))
          .unapply(line)
          .flatMap(_.interOut)
          .map(intersIterator.acceptInterOut)
          .getOrElse(intersIterator.resetInterOut)
      }
    }
  }

}
