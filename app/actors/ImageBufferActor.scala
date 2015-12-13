package actors

import akka.ActorTimer
import akka.actor._

class ImageBufferActor extends Actor with ActorLogging with ActorBuffer with ActorTimer {

  override def preStart(): Unit = {
    setWorker(models.ImageCommons.system, classOf[ImageActor])
    super.preStart()
  }

}
