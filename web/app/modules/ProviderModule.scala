package modules

/**
  * Created by William on 01/01/2016.
  */
import controllers.{
  PlayersProvider,
  ProvidesClanNames,
  ProvidesGames,
  ProvidesServers,
  ProvidesUsers,
  ProvidesUsersList
}
import lib.{ClanDataProvider, ClansProvider}
import play.api.inject.{SimpleModule, bind}
import providers.ReferenceProvider
import providers.full.{FullProvider, PlayersProviderImpl}

class ProviderModule
    extends SimpleModule(
      bind[PlayersProvider].to[PlayersProviderImpl],
      bind[ProvidesServers].to[ReferenceProvider],
      bind[ClanDataProvider].to[FullProvider],
      bind[ClansProvider].to[ReferenceProvider],
      bind[ProvidesGames].to[FullProvider],
      bind[ProvidesClanNames].to[ReferenceProvider],
      bind[ProvidesUsersList].to[ReferenceProvider],
      bind[ProvidesUsers].to[ReferenceProvider]
    )
