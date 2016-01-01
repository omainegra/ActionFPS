import java.io.File
import java.nio.file.Paths

import com.scalawilliam.sfc._
import com.scalawilliam.sfc.Implicits._
import play.api.libs.json.Json

import scala.io.Source

object SampleApp extends App {

  import collection.JavaConverters._

  val gamesFiles = new File("accumulation/src/test/resources/af/samples").listFiles().filter(_.getName.endsWith(".json")).toList
    .map(f => Json.parse(Source.fromFile(f).mkString))

  //    .get("../").iterator().asScala.foreach(println)

  val specLines = "null\n" + gamesFiles.take(5).mkString("\n")

  implicit val fcgi = af.php.Stuff.buildFcgi(9912)

  try {
    val ar = af.php.Stuff.sampleRequest.copy(
      headers = List("Content-Type" -> "text/plain"),
      data = Option(specLines),
      method = "POST"
    )
    val a = ar.process()
    println(a.output)
    val br = ar.copy(
      data = Option(s"${a.output.get}\n" + gamesFiles.drop(5).mkString("\n"))
    )
    val b = br.process()
    println(b.output)
  }
  finally fcgi.destroy()
}
