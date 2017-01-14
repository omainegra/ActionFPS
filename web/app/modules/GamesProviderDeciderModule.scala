package modules

/**
  * Created by William on 01/01/2016.
  */

import com.actionfps.accumulation.GeoIpLookup
import com.actionfps.gameparser.enrichers.IpLookup
import play.api.inject._
import play.api.{Configuration, Environment, Logger, Mode}
import providers.full.{CachedProvider, FullProvider}
import providers.games.{GamesProvider, JournalGamesProvider}

class GamesProviderDeciderModule extends Module {

  val logger = Logger(getClass)

  override def bindings(environment: Environment, configuration: Configuration): List[Binding[_]] = {
    val a =
      if (configuration.getString("af.games.source").contains("journal")) {
        List(bind[GamesProvider].to[JournalGamesProvider])
      } else Nil

    val b =
      if (configuration.getBoolean("af.full.cache").contains(false))
        Nil
      else if (environment.mode == Mode.Dev || configuration.getBoolean("af.full.cache").contains(true)) {
        List(bind[FullProvider].to[CachedProvider])
      } else Nil

    a ++ b ++ List(bind[IpLookup].toInstance(GeoIpLookup))
  }

}

