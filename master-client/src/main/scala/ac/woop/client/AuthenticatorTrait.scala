package ac.woop.client

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef}
import io.enet.akka.ENetService._

trait AuthenticatorTrait { auth: Act with ActorLogging =>
  def service: ActorRef
  def remote: PeerId
  def serverKey: String
  def beginAuthentication(whenAuthenticated: => Unit): Unit = {
    val fsm = AuthFsm(remote, serverKey)
    val sendChallenge = fsm.SendChallenge(fsm.randomChallenge)
    service ! sendChallenge.outputMessage
    log.info(sendChallenge.logMessage)
    become {
      case sendChallenge.CorrectlyIdentified(ack, infoMessage) =>
        log.info(infoMessage)
        service ! ack
      case sendChallenge.ReceivedChallenge(receivedChallenge) =>
        log.info(receivedChallenge.logMessage)
        service ! receivedChallenge.outputMessage
        become {
          case receivedChallenge.AwaitAuthentication(logMessage) =>
            log.info(logMessage)
            whenAuthenticated
        }
      case sendChallenge.WrongResponse(gotResponse, logMessage) =>
        log.info(logMessage)
        // todo throw exception here?
        context stop self
    }
  }
}

