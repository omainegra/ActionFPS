package modules

/**
  * Created by William on 01/01/2016.
  */
import com.google.inject.AbstractModule
import controllers.{ProvidesServers, ProvidesUsers}
import lib.{ClanDataProvider, ClansProvider}
import providers.ReferenceProvider
import providers.full.FullProvider

class ProviderModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[ProvidesServers]).to(classOf[ReferenceProvider])
    bind(classOf[ClanDataProvider]).to(classOf[FullProvider])
    bind(classOf[ClansProvider]).to(classOf[ReferenceProvider])
    bind(classOf[ProvidesUsers]).to(classOf[ReferenceProvider])
  }
}
