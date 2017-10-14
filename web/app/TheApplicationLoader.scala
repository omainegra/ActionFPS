import play.api.{Application, ApplicationLoader}
import play.api.inject.guice.GuiceApplicationLoader

class TheApplicationLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    new GuiceApplicationLoader().load(context)
  }
}
