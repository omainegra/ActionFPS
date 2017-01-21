import java.nio.file.Paths

import com.actionfps.accumulation.user.GeoIpLookup
import com.actionfps.api.Game
import lib.{ForJournal, GamesFromSource}
import org.scalatest.FunSuite
import com.actionfps.formats.json.Formats._
import play.api.Logger
import play.api.libs.json.Json


/**
  * Created by me on 21/01/2017.
  */
class JCTest extends FunSuite {
  ignore("It works") {
    import com.actionfps.accumulation.ReferenceMapValidator.referenceMapValidator
    implicit val ipLookup = GeoIpLookup
    implicit val logger = Logger(getClass)
    val forJournal = ForJournal(Paths.get("/Users/me/af/games-journal.log"))
    forJournal.exist()
    val fs = forJournal.ForSources(
      gameSourceURIs = List.empty, //List(new java.net.URI("https://actionfps.com/all/")),
      serverLogPaths = List.empty
    )
    fs.synchronize()
    println(forJournal.load().size)
    fs.synchronize()
    println(forJournal.load().size)
  }
  test("It loads...") {
    GamesFromSource.loadUnfiltered(scala.io.Source.fromFile(Paths.get("/Users/me/games-journal.log").toFile))
  }
//  test("It loads X") {
//    val str ="""{"id":"2016-10-20T21:50:06Z","gameTime":"2016-10-20T21:50:06","map":"ac_shine","mode":"ctf","state":"match","teams":[{"name":"RVSF","flags":8,"frags":130,"players":[{"name":"IMNT.NarcotiK","host":"93.24.53.21","score":653,"flags":3,"frags":40,"deaths":41},{"name":"IMNT.Krayce","host":"31.38.160.127","score":609,"flags":3,"frags":33,"deaths":35},{"name":"IMNT.kingqu","host":"62.34.82.221","score":789,"flags":2,"frags":57,"deaths":33}]},{"name":"CLA","flags":6,"frags":105,"players":[{"name":"IMNT.SwarM3D*","host":"41.249.83.20","score":895,"flags":5,"frags":52,"deaths":39},{"name":"IMNT.$tOuN3*","host":"41.249.83.20","score":315,"flags":1,"frags":27,"deaths":46},{"name":"w00p|Drakas","host":"103.252.202.13","score":301,"flags":0,"frags":26,"deaths":41}]}],"server":"62-210-131-155.rev.poneytelecom.eu sd-55104 AssaultCube[local#2999]","duration":15}"""
//    println(Json.fromJson[Game](Json.parse(str)))
//  }
}
