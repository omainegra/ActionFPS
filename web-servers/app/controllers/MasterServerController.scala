package controllers

import com.actionfps.servers.ServerRecord
import play.api.Configuration
import play.api.mvc._
import controllers.MasterServerController._

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
class MasterServerController(configuration: Configuration,
                             providesServers: ProvidesServers,
                             components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components) {

  def ms: Action[AnyContent] = Action.async {
    async {
      Ok {
        val addServerMessages =
          await(providesServers.servers).map(addServerMessage)
        (CurrentVersionString :: addServerMessages).mkString(LineSeparator)
      }.as(ContentType)
    }
  }
}

object MasterServerController {
  val LineSeparator = "\n\n"
  val ContentType = "text/plain"
  val CurrentVersionString = "current_version 1000"

  def addServerMessage(serverRecord: ServerRecord): String = {
    s"addserver ${serverRecord.hostname} ${serverRecord.port} ${serverRecord.password.getOrElse("")}"
  }
}
