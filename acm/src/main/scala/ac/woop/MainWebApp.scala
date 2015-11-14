package ac.woop

import ac.woop.MasterClientCentral._
import ac.woop.MasterServer.Repository
import ac.woop.MasterServer.Repository._
import akka.actor.ActorSystem
import akka.util.Timeout
import spray.http.{MediaTypes, MediaType}
import spray.routing.SimpleRoutingApp
import scala.util.Try

import akka.pattern.ask
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import scala.async.Async.{await, async}
import concurrent.duration._
object MainWebApp extends App with SimpleRoutingApp {

  implicit val as = ActorSystem("main")
  import akka.actor.ActorDSL._
  val ctrl = actor(new MasterClientCentral("main.db"))

  implicit val formats = Serialization.formats(NoTypeHints)
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(2.seconds)
  val server = pathPrefix("server" / Segment / IntNumber) { (host: String, port: Int) =>
    put {
      path("key") {
        complete {
          async {
            await(ask(ctrl, CreateNewServerKey(ServerId(host, port))).mapTo[ServerUpdated]) match {
              case ServerUpdated(serverId, serverKeyStr) =>
                write(Map("server" -> Map("id" -> serverId.asString, "key" -> serverKeyStr)))
            }
          }
        }
      } ~ pathEnd {
        complete {
          async {
            await(ask(ctrl, PutServer(ServerId(host, port))).mapTo[ServerUpdated]) match {
              case ServerUpdated(serverId, serverKeyStr) =>
                write(Map("server" -> Map("id" -> serverId.asString, "key" -> serverKeyStr)))
            }
          }
        }
      }
    } ~ delete {
      complete{
        ctrl ! DeleteServer(ServerId(host, port))
        "Deleting server..."
      }
    }
  }
  val user = pathPrefix("user" / Segment) { id =>
    delete {
      complete{
        ctrl ! DeleteUser(UserId(id))
        "Deleting user..."
      }
    } ~
      put {
        path("key") {
          complete {
            async {
              await(ask(ctrl, CreateNewUserKey(UserId(id))).mapTo[UserUpdated]) match {
                case UserUpdated(UserId(userIdStr), UserData(userDataStr), userKey) =>
                  write(Map("user" -> Map("id" -> userIdStr, "data" -> userDataStr, "key" -> userKey)))
              }
            }
          }
        } ~ pathEnd {
          formFields('data) { (data: String) =>
            complete {
              async {
                await(ask(ctrl,PutUser(UserId(id), UserData(data))).mapTo[UserUpdated]) match {
                  case UserUpdated(UserId(userIdStr), UserData(userDataStr), userKey) =>
                    write(Map("user" -> Map("id" -> userIdStr, "data" -> userDataStr, "key" -> userKey)))
                }
              }
            }
          }
        }
      }
  }

  startServer(interface = "0.0.0.0", port = 8765) {
    path("") {
      complete{"hello"}
    } ~ server ~ user ~
    path("repository") {
      get {
        respondWithMediaType(MediaTypes.`application/json`) {
        complete {
          async {
            val repository = await(ask(ctrl, GiveRepository).mapTo[Repository])
            val users = for {
              (UserId(userId), User(key, UserData(data))) <- repository.users
            } yield userId -> Map("id" -> userId, "data" -> data)
            val servers = for {
              (serverId @ ServerId(host, port), Server(key)) <- repository.servers
            }  yield serverId.asString -> Map()
            val mp = Map("users" -> users, "servers" -> servers)
              write(mp)
            }
          }
        }
      }
    } ~ getFromBrowseableDirectory(scala.util.Properties.userDir + java.io.File.separator + "ui")
  }

}