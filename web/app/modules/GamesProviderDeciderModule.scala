package modules

/**
  * Created by William on 01/01/2016.
  */

import com.actionfps.accumulation.GeoIpLookup
import com.actionfps.gameparser.enrichers.IpLookup
import play.api.inject._
import play.api.{Configuration, Environment, Logger, Mode}
import providers.full.{CachedProvider, FullProvider}

import scala.collection.mutable

class GamesProviderDeciderModule extends Module {

  val logger = Logger(getClass)

  override def bindings(environment: Environment, configuration: Configuration): List[Binding[_]] = {
    val bindings = mutable.Buffer.empty[Binding[_]]
    bindings += bind[IpLookup].toInstance(GeoIpLookup)
    if (environment.mode == Mode.Dev || configuration.getBoolean("af.full.cache").contains(true)) {
      bindings += bind[FullProvider].to[CachedProvider]
    }
    bindings.toList
  }

}

