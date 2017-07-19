import org.scalatestplus.play.FakeApplicationFactory
import play.api.inject.DefaultApplicationLifecycle
import play.api.{Application, ApplicationLoader, Configuration, Environment}
import play.core.DefaultWebCommands

/**
  * Created by omainegra on 7/19/17.
  */
trait TheApplicationFactory extends FakeApplicationFactory {
  override def fakeApplication: Application = {
    val env = Environment.simple()
    val configuration = Configuration.load(env)
    val context = ApplicationLoader.Context(
      environment = env,
      sourceMapper = None,
      webCommands = new DefaultWebCommands(),
      initialConfiguration = configuration,
      lifecycle = new DefaultApplicationLifecycle()
    )
    val loader = new TheApplicationLoader()
    loader.load(context)
  }
}
