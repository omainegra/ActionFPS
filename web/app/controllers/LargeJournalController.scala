package controllers

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}

import play.api.Configuration

/**
  * Created by me on 02/05/2017.
  *
  * This is a separate controller for the newly built large journal file.
  * [[RawLogController]] will be replace with this when performance of reading the log
  * is increased significantly.
  */
@Singleton
class LargeJournalController(logFile: Path)
    extends LogController(logFile: Path) {
  @Inject() def this(configuration: Configuration) = this(
    Paths.get(configuration.underlying.getString("journal.large"))
  )
}
