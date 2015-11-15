package services

import actionfps.master.client.sendkeys.ServerUser
import actionfps.master.client.{sendkeys, MasterCClient}
import akka.actor.ActorDSL._
import akka.actor.{Terminated, Kill, ActorRef, Props}
import akka.agent.Agent
import model.{Server, User}
import services.MasterServerActor.{ServerUpdated, UserUpdated}
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import scala.async.Async._
import scala.concurrent.Future

class MasterServerActor(db: JdbcBackend#DatabaseDef) extends Act {

  import context.dispatcher

  val children = Agent(Map.empty[String, (Server, ActorRef)])

  val waitingRefresh = scala.collection.mutable.Set.empty[Server]

  whenStarting {
    db.run(model.servers.result).foreach(_.foreach(loadServer))
  }

  become {
    case u: UserUpdated =>
      println(u)
      async {
        val users = await(db.run(model.users.result)).toList
        val servers = await(db.run(model.servers.result))
        servers.foreach { server =>
          sendKeysToServer(users, server)
        }
      }
    case TerminatedServerAndWaiting(newServer) =>
      loadServer(newServer)
    case TerminatedServer(existingServer) =>
      println("Server went away for some reason?")
    case ExistingServerUpdated(existingActor, newServer) =>
      waitingRefresh += newServer
      existingActor ! Kill
    case s: ServerUpdated =>
      async {
        val users = await(db.run(model.users.result)).toList
        sendKeysToServer(users, s.server)
      }

  }

  object ExistingServerUpdated {
    def unapply(serverUpdated: ServerUpdated): Option[(ActorRef, Server)] = {
      children.get().get(serverUpdated.server.id).map {
        case (_, actor) => actor -> serverUpdated.server
      }
    }
  }

  object TerminatedServer {
    def unapply(message: Terminated): Option[Server] = {
      for {
        (_, (server, actor)) <- children.get()
        if message.actor == actor
      } yield server
    }.headOption
  }

  object TerminatedServerAndWaiting {
    def unapply(message: Terminated): Option[Server] = {
      for {
        (_, (server, actor)) <- children.get()
        if message.actor == actor
        waitingServer <- waitingRefresh
        if waitingServer.id == server.id
      } yield waitingServer
    }.headOption
  }

  def sendKeysToServer(users: List[User], server: Server): Unit = {
    children.get()(server.id)._2 ! sendkeys.SendKeys(
      serverUsers = users.map { user =>
        ServerUser(
          userId = user.id,
          userData = user.data,
          sharedKey = "test"
        )
      }
    )
  }

  def loadServer(server: Server): Future[(Server, ActorRef)] = {
    val props = MasterCClient.MyProps(
      remote = actionfps.master.client.serverIdToPeerId(server.id),
      serverKey = server.key
    )() //(_ => Props(new Act {}))
    // kill if one is already there.
    children.get().get(server.id).foreach(_._2 ! Kill)
    val act = context.actorOf(props, name = server.id)
    context.watch(act)
    children.alter(_.updated(server.id, server -> act)).map(_ => server -> act)
  }

}

object MasterServerActor {

  def props(db: JdbcBackend#DatabaseDef): Props = Props(new MasterServerActor(db))

  case class UserUpdated(user: User)

  case class ServerUpdated(server: Server)

}
