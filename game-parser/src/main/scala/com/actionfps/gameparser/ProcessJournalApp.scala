package com.actionfps.gameparser

import java.io.{FileInputStream, InputStream}
import java.net.URL
import java.util.zip.GZIPInputStream

import com.actionfps.gameparser.mserver.{MultipleServerParser, MultipleServerParserFoundGame}

import scala.io.Codec

/**
  * Created by me on 31/01/2016.
  */
object ProcessJournalApp extends App {

  def parseSource(inputStream: InputStream): Iterator[MultipleServerParserFoundGame] = {
    scala.io.Source.fromInputStream(inputStream)(Codec.UTF8)
      .getLines()
      .scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect { case m: MultipleServerParserFoundGame => m }
  }

  var source = System.in

  Option(System.getProperty("parser.input.file")).foreach { name =>
    source = new FileInputStream(name)
  }

  Option(System.getProperty("parser.input.url")).foreach { name =>
    source = new URL(name).openStream()
  }

  if ("gz".equals(System.getProperty("parser.input.format"))) {
    source = new GZIPInputStream(source)
  }

  parseSource(source)
    .map(g => s"${g.detailString}\n".getBytes("UTF-8"))
    .foreach(b => System.out.write(b))

}
