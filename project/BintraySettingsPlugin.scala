import bintray.BintrayPlugin
import sbt.{AutoPlugin, PluginTrigger}
import bintray.BintrayKeys._

object BintraySettingsPlugin extends AutoPlugin {
  override def requires = BintrayPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings = Seq(
    bintrayRepository := "actionfps"
  )
}
