import com.hazelcast.core.{Hazelcast, HazelcastInstance}
import play.sbt.PlayRunHook
import sbt._

case class HazelcastRunHook() extends PlayRunHook {
  private var hazelcastInstance = Option.empty[HazelcastInstance]

  override def beforeStarted(): Unit = {
    val cfg = new com.hazelcast.config.Config()
    cfg.setInstanceName("hook-hazelcast")
    hazelcastInstance = Option(Hazelcast.getOrCreateHazelcastInstance(cfg))

    super.beforeStarted()
  }

  override def afterStopped(): Unit = {
    hazelcastInstance.foreach(_.shutdown())
    super.afterStopped()
  }
}
