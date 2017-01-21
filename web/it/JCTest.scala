import java.nio.file.Paths

import com.actionfps.accumulation.user.GeoIpLookup
import lib.ForJournal
import org.scalatest.FunSuite
import com.actionfps.formats.json.Formats._
import play.api.Logger


/**
  * Created by me on 21/01/2017.
  */
class JCTest extends FunSuite {
  test("It works") {
    import com.actionfps.accumulation.ReferenceMapValidator.referenceMapValidator
    implicit val ipLookup = GeoIpLookup
    implicit val logger = Logger(getClass)
    val forJournal = ForJournal(Paths.get("/Users/me/af/games-journal-x.log"))
    forJournal.exist()
    val fs = forJournal.ForSources(
      gameSourceURIs = List(new java.net.URI("https://actionfps.com/all/")),
      serverLogPaths = List.empty
    )
    fs.synchronize()
    println(forJournal.load().size)
    fs.synchronize()
    println(forJournal.load().size)
  }
}
