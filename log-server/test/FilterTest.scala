import controllers.LogController
import org.scalatest._
import Matchers._

class FilterTest extends FunSuite {
  test("It filters an IP") {
    LogController.filterOutIp("[1.2.3.45] client connected") shouldEqual "[0.0.0.0] client connected"
  }
  test("It filters out a V2 IP") {
    LogController.filterOutIp("[1.9.3.2:abc] client connected") shouldEqual "[0.0.0.0:abc] client connected"
  }
  test("It filters out a client status IP") {
    LogController.filterOutIp(
      "|DnC|f0r3v3r+         0    0     0  128 normal  1.2.3.4") shouldEqual "|DnC|f0r3v3r+         0    0     0  128 normal  0.0.0.0"
  }
  test("It filters out a client status IP V2") {
    LogController.filterOutIp(
      "|DnC|f0r3v3r+         0    0     0  128 admin 1.2.3.4:x:y") shouldEqual "|DnC|f0r3v3r+         0    0     0  128 admin 0.0.0.0:x:y"
  }
  test("It ignores private messages") {
    assert(
      LogController.ignorePrivateConversation(
        "[12.2.3.4:5] vasa.faik says to vasa.: 'lol wtf' (allowed)"))
  }
}
