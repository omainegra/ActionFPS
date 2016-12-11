package lib

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.actionfps.achievements.immutable.CaptureMaster
import com.actionfps.achievements.immutable.CaptureMapCompletion.Achieving
import com.actionfps.achievements.immutable.CaptureMapCompletion.Achieved
import org.jsoup.Jsoup
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

  def captureMasterRender(captureMaster: CaptureMaster): Html = {
    val doc = Jsoup.parse(wwwLocation.resolve("capture_master.html").toFile, "UTF-8")
    val complete = doc.select("tr.complete").first()
    val incomplete = doc.select("tr.incomplete").first()
    captureMaster.all.map {
      case a@Achieved(map) =>
        val mapName = s"ctf @ ${map}"
        val clone = complete.clone()
        clone.select("th").first().text(mapName)
        clone.select(".cla").first().text(s"${a.cla}/${a.cla}")
        clone.select(".rvsf").first().text(s"${a.rvsf}/${a.rvsf}")
        clone
      case a@Achieving(map, cla, rvsf) =>
        val clone = incomplete.clone()
        val mapName = s"ctf @ ${map}"
        clone.select("th").first().text(mapName)
        clone.select(".cla").first().text(s"$cla/${a.targetPerSide}")
        clone.select(".rvsf").first().text(s"$rvsf/${a.targetPerSide}")
        clone
    }.foreach { clone =>
      incomplete.parents().first().appendChild(clone)
    }
    complete.remove()
    incomplete.remove()
    Html(doc.html())
  }

}
