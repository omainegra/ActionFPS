package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import services.LatestReleaseService

/**
  * Created by me on 04/02/2017.
  */
class DownloadsController @Inject()(latestReleaseService: LatestReleaseService) extends Controller {
  def game = Action { request =>
    val latestRelease = latestReleaseService.latestRelease()
    request.getQueryString("os").collect {
      case "windows" => latestRelease.windowsLink
      case "linux" => latestRelease.linuxLink
      case "mac" => latestRelease.osxLink
    }.flatten match {
      case Some(downloadLink) =>
        SeeOther(downloadLink)
      case None =>
        NotFound("Required 'os' parameter must be one of: 'windows', 'linux', 'mac'.")
    }
  }
}
