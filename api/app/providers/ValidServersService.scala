package providers

/**
  * Created by William on 25/12/2015.
  */

import javax.inject._

import af.ValidServers._

@Singleton
class ValidServersService {

  val validServers = fromResource

}
