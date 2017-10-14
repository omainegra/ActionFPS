import org.scalatestplus.play.FakeApplicationFactory
import play.api.{Application, ApplicationLoader, Environment}

/**
  * Created by omainegra on 7/19/17.
  * For Compile-time DI
  * It use the same ApplicationLoader of the App
  */
trait TheApplicationFactory extends FakeApplicationFactory {
  override def fakeApplication: Application = {
    val context = ApplicationLoader.createContext(Environment.simple())
    val loader = new CompileTimeApplicationLoader()
    loader.load(context)
  }
}
