package actors

import akka.actor._
import akka.pattern.pipe
import play.api.libs.json._
import scala.concurrent.Future
import scala.collection.mutable.Map

object ActorBuffer {
  case class Finished(service: scala.Any, result: scala.Any)
  val PROCESSING: scala.Int = 0
  val FINISHED: scala.Int = 1

  val STARTED_MSG: JsValue = Json.obj("status" -> "started")
  val ACK_MSG: JsValue = Json.obj("status" -> "finished")
  val PROCESSING_MSG: JsValue = Json.obj("status" -> "processing")
  val UNEXPECTED_STATUS_MSG: JsValue = Json.obj("error" -> "Error: Unexpected status found!")
  val UNEXPECTED_FINISH_MSG: JsValue = Json.obj("error" -> "Error: Unexpected finishing reported!")
}

trait ActorBuffer extends Actor {
  import context.dispatcher
  import actors.ActorBuffer._

  case class ServiceResult(Status: Int, Result: scala.Any)
  type BufferResult = Map[scala.Any, ServiceResult]
  var generalBuffer: BufferResult = Map()

  var serviceWorker: ActorRef = _

  def setWorker(system: ActorSystem, workerClass: Class[_ <: akka.actor.Actor]) = {
    serviceWorker = system.actorOf(Props(workerClass), name = self.path.name + "_worker")
  }

  def processBufferGeneral(message: scala.Any): Unit = {
    var result: ServiceResult = null
    message match {
      case Finished(service: scala.Any, resultComplete: scala.Any) => {
        if (generalBuffer.keys.toSet.contains(service)) {
          generalBuffer(service) = ServiceResult(FINISHED, resultComplete)
          Future(ACK_MSG) pipeTo sender
        } else {
          Future(UNEXPECTED_FINISH_MSG) pipeTo sender
        }
      }
      case _ => {
        if (generalBuffer.keys.toSet.contains(message)) {
          val currentRes = generalBuffer(message)
          if (currentRes.Status == PROCESSING) {
            Future(PROCESSING_MSG) pipeTo sender
          } else if (currentRes.Status == FINISHED) {
            result = generalBuffer(message)
            generalBuffer = generalBuffer - message
            Future(result.Result) pipeTo sender
          } else {
            Future(UNEXPECTED_STATUS_MSG) pipeTo sender
          }
        } else {
          generalBuffer = generalBuffer ++ Map(message -> ServiceResult(PROCESSING, null))
          serviceWorker ! message
          Future(STARTED_MSG) pipeTo sender
        }
      }
    }
  }

  override def receive = {
    case srv => processBufferGeneral(srv)
  }

}