import org.scalatest.FunSuite
import services.NewsService
import concurrent.ExecutionContext.Implicits.global

/**
  * Created by me on 25/01/2017.
  */
class NewsServiceTest extends FunSuite {
  test("It doesn't crash") {
    val ns = new NewsService()
    val result = ns.latestItem()
    info(s"$result")
  }
}
