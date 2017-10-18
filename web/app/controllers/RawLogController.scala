package controllers

import java.nio.file.Path

import play.api.mvc.ControllerComponents

/**
  * Created by me on 02/05/2017.
  */
class RawLogController(logFile: Path, components: ControllerComponents)
    extends LogController(logFile.toAbsolutePath, components)
