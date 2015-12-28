package controllers

import java.io.File

import play.api.{Mode, Play}
import play.api.mvc.{Action, Controller}

/**
  * Created by William on 28/12/2015.
  */
object Diasse extends Controller {
  import Play.current
  def allFiles = {
    val x = Play.application.getFile("www")
    getRecursiveListOfFiles(x).map { f =>
      f.getCanonicalPath.substring(x.getCanonicalPath.length + 1).replaceAllLiterally("\\", "/") -> f
    }
  }
  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }
  def get(at: String) = {
    Play.mode match {
      case Mode.Prod => Action {
//        allFiles.foreach(println)
        allFiles.collectFirst {
          case (`at`, file) => file
        } match {
          case Some(file) => Ok.sendFile(content = file, inline=true)
          case None => NotFound
        }
      }
      case _ =>
        ExternalAssets.at(rootPath = "dist/www", file = at)
    }
  }
}
