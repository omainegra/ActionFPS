package lib

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.actionfps.achievements.immutable.CaptureMaster
import com.actionfps.achievements.immutable.CaptureMapCompletion.Achieving
import com.actionfps.achievements.immutable.CaptureMapCompletion.Achieved
import org.jsoup.nodes.Element
import play.twirl.api.Html


/**
  * Created by me on 11/12/2016.
  */
object Soup {
  lazy val wwwLocation: Path = {
    List("web/dist/www", "dist/www", "www")
      .map(item => Paths.get(item))
      .find(path => Files.exists(path))
      .getOrElse {
        throw new IllegalArgumentException(s"Could not find 'www'.")
      }
  }
  var f = new File("web/dist/www/template.html")
  if (!f.exists()) {
    f = new File("www/template.html")
  }
  if (!f.exists()) {
    f = new File("dist/www/template.html")
  }

}
