package providers

import java.io.File

import acleague.enrichers.JsonGame
import lib.GameTailer
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by William on 26/12/2015.
  */
trait TailsGames {
  def configuration: Configuration

  def applicationLifecycle: ApplicationLifecycle

  def file = new File(configuration.underlying.getString("af.games.path"))

  def processGame(game: JsonGame): Unit

  def validServersService: ValidServersService

  def initialiseTailer(fromStart: Boolean): GameTailer = {
    val tailer = new GameTailer(validServersService.validServers, file, !fromStart)(processGame)
    applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))
    tailer
  }
}
