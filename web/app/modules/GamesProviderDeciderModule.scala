package modules

/**
  * Created by William on 01/01/2016.
  */
import play.api.{Configuration, Environment, Logger}

import play.api.inject._
import providers.games.{GamesProvider, JournalGamesProvider}

class GamesProviderDeciderModule extends Module {

  val logger = Logger(getClass)

  override def bindings(environment: Environment, configuration: Configuration) = {
    if ( configuration.getString("af.games.source").contains("journal") ) {
      List(bind[GamesProvider].to[JournalGamesProvider])
    } else Nil
  }

}

