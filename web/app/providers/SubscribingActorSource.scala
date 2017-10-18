package providers

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source

import scala.reflect.ClassTag

object SubscribingActorSource {

  def apply[T](bufferSize: Int)(implicit actorSystem: ActorSystem,
                                ev: ClassTag[T]): Source[T, Boolean] = {
    Source
      .actorRef[T](bufferSize = bufferSize, OverflowStrategy.dropBuffer)
      .mapMaterializedValue(
        actorSystem.eventStream.subscribe(_, ev.runtimeClass))
  }

}
