package tl

import java.io.InputStreamReader
import java.util.function.Consumer
import javax.script.ScriptEngineManager

import scala.collection.mutable

/**
  * Created by me on 31/12/2016.
  */
case class OpenMatchPlayers(matchId: Int, first: String, second: String)

object OpenMatchPlayers {
  private val scriptingEngine = new ScriptEngineManager().getEngineByName("javascript")
  scriptingEngine.eval(new InputStreamReader(getClass.getResourceAsStream("extract.js")))

  def fromJsonString(jsonString: String): List[OpenMatchPlayers] = this.synchronized {
    val buffer = mutable.Buffer.empty[OpenMatchPlayers]
    val cons = new Consumer[OpenMatchPlayers] {
      override def accept(t: OpenMatchPlayers): Unit = buffer.append(t)
    }
    scriptingEngine.put("inputJson", jsonString)
    scriptingEngine.put("cons", cons)
    scriptingEngine.eval("for each (i in Tournament.fromJSON(inputJson).getOpenMatchesPlayers()) cons(i);")
    buffer.toList
  }
}
