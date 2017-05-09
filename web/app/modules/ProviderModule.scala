package modules

/**
  * Created by William on 01/01/2016.
  */
import com.google.inject.AbstractModule
import controllers.{PlayersProvider, ProvidesClanNames, ProvidesGames, ProvidesServers, ProvidesUsers, ProvidesUsersList}
import lib.{ClanDataProvider, ClansProvider}
import providers.ReferenceProvider
import providers.full.{FullProvider, PlayersProviderImpl}

class ProviderModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[PlayersProvider]).to(classOf[PlayersProviderImpl])
    bind(classOf[ProvidesServers]).to(classOf[ReferenceProvider])
    bind(classOf[ClanDataProvider]).to(classOf[FullProvider])
    bind(classOf[ClansProvider]).to(classOf[ReferenceProvider])
    bind(classOf[ProvidesGames]).to(classOf[FullProvider])
    bind(classOf[ProvidesClanNames]).to(classOf[ReferenceProvider])
    bind(classOf[ProvidesUsersList]).to(classOf[ReferenceProvider])
    bind(classOf[ProvidesUsers]).to(classOf[ReferenceProvider])
  }
}
