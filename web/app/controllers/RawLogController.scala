package controllers

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

import lib.ForJournal
import play.api.Configuration

/**
  * Created by me on 02/05/2017.
  */
@Singleton
class RawLogController(logFile: Path) extends LogController(logFile.toAbsolutePath: Path) {
  @Inject() def this(configuration: Configuration) = this(
    ForJournal.ForConfig(configuration.underlying).logJournalPath
  )
}
