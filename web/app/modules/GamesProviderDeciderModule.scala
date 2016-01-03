package modules

/**
  * Created by William on 01/01/2016.
  */
import play.api.{Mode, Configuration, Environment, Logger}

import play.api.inject._
import providers.full.{CachedProvider, FullProvider}
import providers.games.{GamesProvider, JournalGamesProvider}

class GamesProviderDeciderModule extends Module {

  val logger = Logger(getClass)

  override def bindings(environment: Environment, configuration: Configuration) = {
    val a = if ( configuration.getString("af.games.source").contains("journal") ) {
      List(bind[GamesProvider].to[JournalGamesProvider])
    } else Nil

    val b = if ( environment.mode == Mode.Dev ) {
      List(bind[FullProvider].to[CachedProvider])
    } else Nil

    a ++ b
  }

}

