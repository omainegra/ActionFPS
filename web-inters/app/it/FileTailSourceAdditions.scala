package it

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}

import akka.NotUsed
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.duration.FiniteDuration

object FileTailSourceAdditions {
  implicit class RichFileTailSource(fileTailSource: FileTailSource.type) {
    def newLines(
        path: Path,
        maxLineSize: Int,
        pollingInterval: FiniteDuration,
        lf: String = System.getProperty("line.separator"),
        charset: Charset = StandardCharsets.UTF_8): Source[String, NotUsed] = {
      val startPosition = Files.size(path)
      fileTailSource
        .apply(path, maxLineSize, startPosition, pollingInterval)
        .via(
          akka.stream.scaladsl.Framing.delimiter(
            ByteString.fromString(lf, charset.name),
            maxLineSize,
            allowTruncation = false))
        .map(_.decodeString(charset))
    }
  }
}
