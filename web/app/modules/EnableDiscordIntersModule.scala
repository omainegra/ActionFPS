package modules

/**
  * Created by William on 01/01/2016.
  */

import com.google.inject.AbstractModule
import services.DiscordInters

class EnableDiscordIntersModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[DiscordInters]).asEagerSingleton()
  }
}
