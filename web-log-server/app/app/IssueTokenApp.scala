package app

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import controllers.LogController
import controllers.LogController.LogAccess

/**
  * Usage:
  * $ sbt stage
  * $ ./web-log-server/target/universal/stage/bin/weblogserver -main app.IssueTokenApp -Dconfig.file=/path/to/config.conf -Dlevels=ip,old -Dexpires=100d
  * $ ./web/target/universal/stage/bin/web -main app.IssueTokenApp -Dconfig.file=/path/to/config.conf -Dlevels=ip,old -Dexpires=100d
  *
  * Or anything similar. Note 'd' is the highest time unit you can use.
  */
object IssueTokenApp extends App {

  private val config = ConfigFactory.load()
  private val jwt = LogController.issueJwt(
    key = config
      .getString("play.http.secret.key")
      .ensuring(_ != "changeme", "using default secret key"),
    logAccess = LogAccess(config.getString("levels").split(",").toSet),
    expireSeconds = config.getDuration("expires", TimeUnit.SECONDS)
  )

  println(jwt)

}
