package services

import actionfps.master.client.MasterCClient
import akka.actor.ActorDSL._
import akka.actor.Props
import model.{Server, User}
import services.MasterServerActor.{ServerUpdated, UserUpdated}
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend

class MasterServerActor(db: JdbcBackend#DatabaseDef) extends Act {
  become {
    case u: UserUpdated =>
      println(u)
    case s: ServerUpdated =>
      println(s)
  }
//
//  db.run(model.servers.result).map(_.map {
//    case (serverId, serverKey) =>
//      val props = MasterCClient.MyProps(
//        remote = actionfps.master.client.serverIdToPeerId(serverId),
//        serverKey = serverKey
//      )(_ => Props(new Act{}))
//      context.actorOf(props, name = serverId)
//  }) pipeTo self

}
object MasterServerActor {
  def props(db: JdbcBackend#DatabaseDef): Props = Props(new MasterServerActor(db))

  case class UserUpdated(user: User)
  case class ServerUpdated(server: Server)
}
