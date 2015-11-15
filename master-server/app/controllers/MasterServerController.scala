package controllers

import javax.inject._
import actionfps.master.client.MasterCClient
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.agent.Agent
import model.{Server, User}
import play.api.db.slick.DatabaseConfigProvider
import play.api.http.Writeable
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.MasterServer
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class MasterServerController @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                      masterServer: MasterServer)
                                      (implicit executionContext: ExecutionContext,
                             actorSystem: ActorSystem) extends Controller {

  import slick.driver.PostgresDriver.api._
  val db = dbConfigProvider.get[JdbcProfile].db
  db.run(model.users.schema.create)
  db.run(model.servers.schema.create)
  import scala.async.Async._

  implicit val uWr = Json.writes[User]
  implicit val sWr = Json.writes[Server]
  implicit val userWritable = Writeable.writeableOf_JsValue.map[User](u =>Json.toJson(u))
  implicit val serverWritable = Writeable.writeableOf_JsValue.map[Server](s=>Json.toJson(s))

  import org.scalactic._
  def updateUserData(id: String) = Action.async { request =>
    val context = masterServer.UserContext(userId = id)
    val userData = request.body.asText.get
    async {
      await(context.putData(userData = userData)) match {
        case Some(user) => Ok(user)
        case None => NotFound("User not found")
      }
    }
  }
  def refreshUserKey(id: String, updateId: String) = Action.async { request =>
    val context = masterServer.UserContext(userId = id)
    async {
      await(context.refreshKey(updateId = updateId)) match {
        case Some(user) => Ok(user)
        case None => NotFound("User not found")
      }
    }
  }
  def createUser(id: String) = Action.async { request =>
    val context = masterServer.UserContext(userId = id)
    val userData = request.body.asText.get
    async {
      Ok(await(context.create(userData)))
    }
  }

  def createServer(id: String) = Action.async { request =>
    val context = masterServer.ServerContext(serverId = id)
    async {
      Ok(await(context.create()))
    }
  }

  def refreshServerKey(id: String, updateId: String) = Action.async { request =>
    val context = masterServer.ServerContext(serverId = id)
    async {
      await(context.refreshKey(updateId = updateId)) match {
        case Some(server) => Ok(server)
        case None => NotFound("Server not found")
      }
    }
  }

//  db.run(model.servers.schema.create).onComplete{case x => println(x)}
//  db.run(model.users.schema.create).onComplete{case x => println(x)}

/*
Option(users.toList.map{case (userId, userKey, userData) =>
          ServerUser.build(
            userId = userId,
            userKey = userKey,
            serverId = serverId,
            userData = userData
          )
        })
 */


  def main = TODO

//  actorSystem.actorOf(MasterCClient.MyProps())
}