package modules

/**
  * Created by William on 01/01/2016.
  */
import play.api.inject.{SimpleModule, _}
import services.ChallongeService

class ChallongeLoadModule extends SimpleModule(bind[ChallongeService].toSelf.eagerly())
