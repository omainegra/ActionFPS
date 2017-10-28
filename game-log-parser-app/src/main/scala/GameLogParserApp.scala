import com.actionfps.gameparser.GameScanner

/**
  * Created by me on 14/01/2017.
  */
object GameLogParserApp extends App {

  /**
    * Accepted input is of the format is the syslog format:
    * Date: 2016-05-14T07:26:56.219Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: [103.252.202.214] w00p|Drakas says: 'kk'
    * **/
  val source = scala.io.Source.fromURI(
    new java.net.URI(System.getProperty("source", "file:///dev/stdin")))
  try source
    .getLines()
    .scanLeft(GameScanner.initial)(GameScanner.scan)
    .collect(GameScanner.collect)
    .take(5)
    .foreach(println)
  finally source.close
}
