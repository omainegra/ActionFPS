package controllers

import com.actionfps.servers.ServerRecord
import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ProvidesServers {
  def servers: Future[List[ServerRecord]]
}
