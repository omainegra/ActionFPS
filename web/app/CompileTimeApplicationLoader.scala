import java.nio.file.{Path, Paths}

import af.inters.{DiscordInters, OneSignalInters}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import com.actionfps.accumulation.ReferenceMapValidator
import com.actionfps.accumulation.user.GeoIpLookup
import com.actionfps.gameparser.enrichers.{IpLookup, MapValidator}
import com.softwaremill.macwire._
import controllers.{
  Admin,
  AllGames,
  ClansController,
  DownloadsController,
  EventStreamController,
  Forwarder,
  GamesController,
  IndexController,
  LadderController,
  MasterServerController,
  PlayersController,
  PlayersProvider,
  RawLogController,
  ServersController,
  StaticPageRouter,
  UserController,
  VersionController
}
import inters.IntersController
import lib.WebTemplateRender
import play.api.ApplicationLoader.Context
import play.api.Configuration
import play.api.cache.ehcache.EhCacheComponents
import play.api.http.FileMimeTypes
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents
import play.filters.gzip.GzipFilterComponents
import providers.{ReferenceProvider, SubscribingActorSource}
import providers.full.{
  FullProvider,
  FullProviderImpl,
  HazelcastCachedProvider,
  PlayersProviderImpl
}
import providers.games.GamesProvider
import router.Routes
import services.ChallongeService.NewClanwarCompleted
import services._
import tl.ChallongeClient

final class CompileTimeApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context): play.api.Application =
    new CompileTimeApplicationLoaderComponents(context).application
}

final class CompileTimeApplicationLoaderComponents(context: Context)
    extends play.api.BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with EhCacheComponents
    with GzipFilterComponents
    with AhcWSComponents
    with CORSComponents
    with _root_.controllers.AssetsComponents {

  override def httpFilters: Seq[EssentialFilter] = Seq(corsFilter, gzipFilter)
  lazy val journalPath: Path =
    Paths.get(configuration.get[String]("journal.large"))
  implicit lazy val as: ActorSystem = this.actorSystem
  implicit lazy val mimeTypes: FileMimeTypes = this.fileMimeTypes
  implicit lazy val config: Configuration = this.configuration
  implicit lazy val ws: WSClient = this.wsClient
  lazy val env: play.Environment = this.environment.asJava
  lazy val webTemplateRender: WebTemplateRender = wire[WebTemplateRender]
  lazy val newsService: NewsService = wire[NewsService]
  lazy val allGames: AllGames = wire[AllGames]
  lazy val playersProvider: PlayersProvider = wire[PlayersProviderImpl]
  implicit lazy val referenceProvider: ReferenceProvider =
    new ReferenceProvider(configuration, defaultCacheApi)(wsClient,
                                                          executionContext)
  lazy val gamesProvider: GamesProvider = wire[GamesProvider]
  lazy val forwarder: Forwarder = wire[Forwarder]
  lazy val gamesController: GamesController = wire[GamesController]
  lazy val indexController: IndexController = wire[IndexController]
  implicit lazy val ipLookup: IpLookup = GeoIpLookup
  lazy val clansController: ClansController = wire[ClansController]
  lazy val playersController: PlayersController = wire[PlayersController]
  lazy val ladderController: LadderController = wire[LadderController]
  lazy val serversController: ServersController = wire[ServersController]
  lazy val pingerService: PingerService = wire[PingerService]
  lazy val admin: Admin = wire[Admin]
  lazy val versionController: VersionController = wire[VersionController]
  lazy val userController: UserController = wire[UserController]
  lazy val latestReleaseService: LatestReleaseService =
    wire[LatestReleaseService]
  lazy val downloadsController: DownloadsController = wire[DownloadsController]
  lazy val RawLogController: RawLogController = wire[RawLogController]
  lazy val eventStreamController: EventStreamController =
    wire[EventStreamController]
  lazy val masterServerController: MasterServerController =
    wire[MasterServerController]
  lazy val intersController: IntersController =
    wire[IntersController]
  lazy val fullProvider: FullProvider = {
    val fullProviderImpl = wire[FullProviderImpl]
    if (useCached)
      new HazelcastCachedProvider(fullProviderImpl)(executionContext)
    else fullProviderImpl
  }
  private lazy val useCached =
    configuration
      .getOptional[String]("full.provider")
      .contains("hazelcast-cached")
  implicit lazy val mapValidator: MapValidator =
    ReferenceMapValidator.referenceMapValidator
  lazy val staticPageRouter: StaticPageRouter = wire[StaticPageRouter]
  lazy val prefix: String = "/"
  lazy val router: Routes = wire[Routes]

  configuration
    .getOptional[Configuration]("challonge")
    .filter(_.get[Boolean]("enabled"))
    .map(ChallongeClient.apply)
    .foreach { challongeClient =>
      SubscribingActorSource[NewClanwarCompleted](10)
        .via(ChallongeService.sinkFlow(challongeClient))
        .toMat(Sink.ignore)(Keep.right)
        .run()
        .onComplete(intersService.completionHandler)
    }

  lazy val challongeClient: ChallongeClient = wire[ChallongeClient]
  lazy val challongeEnabled: Boolean = configuration
    .get[Seq[String]]("play.modules.enabled")
    .contains("modules.ChallongeLoadModule")

  lazy val intersService: IntersService =
    new IntersService(
      journalPath = journalPath
    )(() => referenceProvider.users, executionContext, actorSystem)

  intersService.beginPushing()

  configuration
    .getOptional[Configuration]("one-signals")
    .map(OneSignalInters(_))
    .foreach { oneSignal =>
      intersService
        .newIntersSource("OneSignalsReader")
        .toMat(oneSignal.pushSink)(Keep.right)
        .run()
        .onComplete(intersService.completionHandler)
    }

  configuration
    .getOptional[Configuration]("discord")
    .map(DiscordInters(_))
    .foreach { discord =>
      intersService
        .newIntersSource("DiscordReader")
        .toMat(discord.pushSink)(Keep.right)
        .run()
        .onComplete(intersService.completionHandler)
    }

}
