package modules

/**
  * Created by William on 01/01/2016.
  */

import com.google.inject.AbstractModule
import services.ChallongeService

class EagerLoadModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[ChallongeService]).asEagerSingleton()
  }
}
