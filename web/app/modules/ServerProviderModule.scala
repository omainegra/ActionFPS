package modules

/**
  * Created by William on 01/01/2016.
  */

import com.google.inject.AbstractModule
import controllers.ProvidesServers
import providers.ReferenceProvider

class ServerProviderModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[ProvidesServers]).to(classOf[ReferenceProvider])
  }
}
