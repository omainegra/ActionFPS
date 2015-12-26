package services

/**
  * Created by William on 25/12/2015.
  */
import javax.inject._

import lib.validservers.ValidServers

@Singleton
class ValidServersService {
  val validServers = ValidServers.fromResource
}
