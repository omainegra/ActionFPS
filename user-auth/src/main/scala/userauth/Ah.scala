package userauth

import java.nio.file.{Files, Path, Paths}
import java.security.{KeyPairGenerator, SecureRandom}
import java.util.stream.Collectors
import javax.xml.bind.DatatypeConverter

object Ah {

  // https://docs.oracle.com/javase/tutorial/security/apisign/step2.html

  def parseValues(line: String): Map[String, List[String]] = {
    line
      .split(" ")
      .toList
      .map(_.split("=").toList)
      .collect {
        case k :: v :: Nil => k -> v
      }
      .groupBy(_._1)
      .mapValues(_.map(_._2))
  }

  def serializeValues(map: Map[String, List[String]]): String = {
    map
      .toList
      .flatMap { case (k, vs) => vs.map { v => k -> v } }
      .sortBy(_._2.length)
      .map { case (k, v) => s"$k=$v" }
      .filterNot(_.contains(" "))
      .mkString(" ")
  }

  def generatePair(): (String, String) = {
    val kpg = KeyPairGenerator.getInstance("DSA")
    val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
    kpg.initialize(512, random)
    val pair = kpg.generateKeyPair()
    val priv = pair.getPrivate
    val pub = pair.getPublic
    (DatatypeConverter.printHexBinary(priv.getEncoded), DatatypeConverter.printHexBinary(pub.getEncoded))
  }

  import collection.JavaConverters._

  def idOf(params: Map[String, List[String]]): Option[String] = {
    PartialFunction.condOpt(params.get("id")) {
      case Some(id :: Nil) => id
    }
  }

  def getUser(id: String, path: Path): Option[Map[String, List[String]]] = {
    Files
      .lines(path)
      .collect(Collectors.toList[String])
      .asScala
      .toList
      .map(parseValues)
      .find(p => idOf(p).contains(id))
  }

  def putUser(id: String, path: Path, params: Map[String, List[String]]): Unit = synchronized {
    require(params.get("id").contains(List(id)) && params.get("id").exists(_.length == 1))
    val allLines = Files
      .lines(path)
      .collect(Collectors.toList[String])
      .asScala
      .toList
    val otherLines = allLines.filterNot { line => idOf(parseValues(line)).contains(id) }
    val newLine = serializeValues(params)
    val newLines = otherLines :+ newLine :+ ""
    Files.write(path, newLines.mkString("\n").getBytes())
  }

  // https://gist.github.com/Paul255/6c4dec4f13ef03f6579f
}
