package controllers

import javax.inject._
import ac.woop.client.MasterCClient
import akka.actor.ActorSystem
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Controller
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class MasterServer @Inject()(dbConfigProvider: DatabaseConfigProvider)
                            (implicit executionContext: ExecutionContext,
                             actorSystem: ActorSystem) extends Controller{

  val db = dbConfigProvider.get[JdbcProfile].db

  import slick.driver.PostgresDriver.api._
//  db.run(model.servers.schema.create).onComplete{case x => println(x)}
//  db.run(model.users.schema.create).onComplete{case x => println(x)}
  db.run(model.servers.result).foreach{servers =>
    servers foreach println
    servers.foreach {
      case (id, key) =>
        val props = MasterCClient.MyProps(
          remote = ac.woop.client.serverIdToPeerId(id),
          serverKey = key,
          exchangeKeysImmediately = Option.empty
        )()
        actorSystem.actorOf(props)
    }
  }

  def main = TODO

//  actorSystem.actorOf(MasterCClient.MyProps())
}