import java.io.File
import java.nio.file.Paths

import com.scalawilliam.sfc._
import com.scalawilliam.sfc.Implicits._
import play.api.libs.json.Json

import scala.io.Source

object SampleApp extends App {
  val sampleRequest = FastCGIRequest(
    remoteUser = None,
    headers = List.empty,
    authType = Option.empty,
    queryString = None,
    contextPath = "",
    servletPath = "/test.php",
    requestURI = "/uri/",
    serverName = "ScalaTest",
    protocol = "HTTP/1.1",
    remoteAddr = "127.0.0.1",
    serverPort = 1234,
    method = "GET",
    data = None,
    realPath = something => {
      scala.util.Properties.userDir + "/php-client/src/main/resources/root" + something
    }
  )

  import collection.JavaConverters._

  val gamesFiles = new File("accumulation/src/test/resources/af/samples").listFiles().filter(_.getName.endsWith(".json")).toList
    .map(f => Json.parse(Source.fromFile(f).mkString))

  //    .get("../").iterator().asScala.foreach(println)

  val specLines = "null\n" + gamesFiles.take(5).mkString("\n")

  implicit val fcgi = FastCGIHandlerConfig(
    connectionConfig = FastCGIConnectionConfig.SingleConnection(
      address = "127.0.0.1:7772"
    ),
    startExecutable = Option(s"C:/php/php-cgi.exe -b 127.0.0.1:7772")
  ).build
  try {
    val ar = sampleRequest.copy(
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
