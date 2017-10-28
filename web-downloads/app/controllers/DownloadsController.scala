package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}
import services.LatestReleaseService

/**
  * Created by me on 04/02/2017.
  */
class DownloadsController(latestReleaseService: LatestReleaseService,
                          components: ControllerComponents)
    extends AbstractController(components) {
  def game = Action { request =>
    val latestRelease = latestReleaseService.latestRelease()
    request.getQueryString("os").collect {
      case "windows" => latestRelease.windowsLink
      case "linux" => latestRelease.linuxLink
      case "mac" => latestRelease.osxLink
    } match {
      case Some(Some(downloadLink)) =>
        SeeOther(downloadLink)
      case Some(None) =>
        NotFound(
          "Could not find a download link for your OS. Contact us if issue persists.")
      case None =>
        NotFound(
          "Required 'os' parameter must be one of: 'windows', 'linux', 'mac'.")
    }
  }
}
