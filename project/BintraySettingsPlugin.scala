import bintray.BintrayPlugin
import sbt.AutoPlugin
import bintray.BintrayKeys._

object BintraySettingsPlugin extends AutoPlugin {
  override def requires = BintrayPlugin

  override def trigger = allRequirements

  override def projectSettings = Seq(
    bintrayRepository := "actionfps"
  )
}