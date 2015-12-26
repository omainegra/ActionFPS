package acleague

import java.io.{FileOutputStream, InputStream, FileInputStream}

import acleague.mserver.{MultipleServerParser, MultipleServerParserFoundGame}

import scala.io.Codec


object ProcessJournalApp extends App {

  def parseSource(inputStream: InputStream): Iterator[MultipleServerParserFoundGame] = {
    scala.io.Source.fromInputStream(inputStream)(Codec.UTF8)
      .getLines()
      .scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect { case m: MultipleServerParserFoundGame => m }
  }

  parseSource(System.in)
    .map(g => s"${g.detailString}\n".getBytes("UTF-8"))
    .foreach(b => System.out.write(b))

}
