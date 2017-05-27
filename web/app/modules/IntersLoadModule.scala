package modules

/**
  * Created by William on 01/01/2016.
  */
import play.api.inject.{SimpleModule, bind}
import services.IntersService

class IntersLoadModule
    extends SimpleModule(bind[IntersService].toSelf.eagerly())
