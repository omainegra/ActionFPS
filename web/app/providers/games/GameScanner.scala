package providers.games

import af.streamreaders.{IteratorTailerListenerAdapter, Scanner, TailedScannerReader}
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.gameparser.mserver.{ExtractMessage, MultipleServerParser, MultipleServerParserFoundGame}

/**
  * Created by me on 14/05/2016.
  */
object GameScanner extends Scanner[String, MultipleServerParser] {

  def zero = MultipleServerParser.empty

  def scan(state: MultipleServerParser, line: String): MultipleServerParser = {
    line match {
      case line@ExtractMessage(date, _, _) => state.process(line)
      case _ => state
    }
  }

  def collect: PartialFunction[MultipleServerParser, JsonGame] = {
    case MultipleServerParserFoundGame(fg, _) => fg
  }

  def tailReader(adapter: IteratorTailerListenerAdapter) = new TailedScannerReader(adapter, this)
}
