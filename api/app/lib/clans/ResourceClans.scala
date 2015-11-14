package lib.clans

import scala.io.Codec

object ResourceClans extends Clans {
  override lazy val yaml = {
    val clansIs = getClass.getResourceAsStream("/clans.yaml")
    try scala.io.Source.fromInputStream(clansIs)(Codec.UTF8).getLines().mkString("\n")
    finally clansIs.close()
  }
}
