package ac.woop

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp

/**
 * Created by William on 15/02/2015.
 */
object DemoThingyApp extends App with SimpleRoutingApp {

  implicit val as = ActorSystem("main")

  startServer(interface = "0.0.0.0", port = 5567) {
    getFromBrowseableDirectory(scala.util.Properties.userDir + java.io.File.separator + "ui")
  }

}
