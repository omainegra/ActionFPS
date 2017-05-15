package af

import java.io.RandomAccessFile
import java.nio.file.Path

import scala.annotation.tailrec

case class FileOffsetFinder(target: String) {

  def apply(raf: RandomAccessFile): Long = {
    raf.seek(0)
    val line = raf.readLine()
    // early finish
    if (line == null || target <= line) 0
    else {
      @tailrec
      def go(min: Long, max: Long): Long =
        if (min <= max) {
          // go to midpoint
          val mid = min + (max - min) / 2
          raf.seek(mid)
          // skip one (partial) line
          raf.readLine()
          // so we can get to a full line
          val line = raf.readLine()
          // change the bounds accordingly
          if (line == null || target <= line)
            go(min, mid - 1)
          else
            go(mid + 1, max)
        } else {
          // we've reached our target which is at least the second line
          raf.seek(min)
          raf.readLine
          raf.getFilePointer
        }
      go(0, raf.length())
    }
  }

  def apply(path: Path): Long = {
    val raf = new RandomAccessFile(path.toFile, "r")
    try {
      apply(raf)
    } finally raf.close()
  }
}
