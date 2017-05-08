package modules

/**
  * Created by William on 01/01/2016.
  */

import com.google.inject.AbstractModule
import controllers.{ProvidesServers, ProvidesUsers}
import providers.ReferenceProvider

class ReferenceProviderModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[ProvidesServers]).to(classOf[ReferenceProvider])
    bind(classOf[ProvidesUsers]).to(classOf[ReferenceProvider])
  }
}
