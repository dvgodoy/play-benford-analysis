package actors

import akka.ActorTimer
import akka.actor._

class BenfordBufferActor extends Actor with ActorLogging with ActorBuffer with ActorTimer {

  override def preStart(): Unit = {
    setWorker(models.BenfordCommons.system, classOf[BenfordActor])
    super.preStart()
  }

}
