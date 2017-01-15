package com.actionfps.pinger


import akka.actor.ActorDSL._
import akka.actor.SupervisorStrategy.Decider
import akka.actor.{ActorKilledException, ActorLogging, Kill, Props, SupervisorStrategy}
/**
  * Created by me on 14/01/2017.
  */
class ListenerActor(g: ServerStatus => Unit, h: CurrentGameStatus => Unit)
                   (implicit serverMappings: ServerMappings) extends Act with ActorLogging {

  import context.dispatcher

  log.info("Starting listener actor for pinger service...")

  private val pingerActor = context.actorOf(name = "pinger", props = Pinger.props)

  override final val supervisorStrategy: SupervisorStrategy = {
    def defaultDecider: Decider = {
      case _: ActorKilledException ⇒ Restart
      case _: Exception ⇒ Restart
    }

    OneForOneStrategy()(defaultDecider)
  }

  import concurrent.duration._

  context.system.scheduler.schedule(10.minutes, 10.minutes, pingerActor, Kill)

  become {
    case sp: SendPings =>
      pingerActor ! sp
    case a: ServerStatus =>
      g(a)
    case b: CurrentGameStatus =>
      h(b)
  }
}

object ListenerActor {
  def props(g: ServerStatus => Unit, h: CurrentGameStatus => Unit) = Props(new ListenerActor(g, h))
}
