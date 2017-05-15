package controllers

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

import lib.ForJournal
import play.api.Configuration
import play.api.mvc.ControllerComponents

/**
  * Created by me on 02/05/2017.
  */
@Singleton
class RawLogController(logFile: Path, components: ControllerComponents)
    extends LogController(logFile.toAbsolutePath, components) {
  @Inject()
  def this(configuration: Configuration, components: ControllerComponents) =
    this(
      ForJournal.ForConfig(configuration.underlying).lastLogPathO.get,
      components
    )
}
