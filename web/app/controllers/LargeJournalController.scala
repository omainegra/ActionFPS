package controllers

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc.ControllerComponents

/**
  * Created by me on 02/05/2017.
  */
@Singleton
class LargeJournalController(logFile: Path,
                             controllerComponents: ControllerComponents)
    extends LogController(logFile: Path, controllerComponents) {
  @Inject()
  def this(configuration: Configuration,
           controllerComponents: ControllerComponents) = this(
    Paths.get(configuration.underlying.getString("journal.large")),
    controllerComponents
  )
}
