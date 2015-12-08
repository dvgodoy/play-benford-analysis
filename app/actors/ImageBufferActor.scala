package actors

import akka.ActorTimer
import akka.actor._
import models.ImageCommons._

class ImageBufferActor extends Actor with ActorLogging with ActorBuffer with ActorTimer {

  setWorker(system, classOf[ImageActor])

}
