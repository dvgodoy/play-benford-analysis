package actors

import akka.actor._
import akka.pattern.pipe
import play.api.libs.json._
import scala.concurrent.Future
import scala.collection.mutable.Map

object ActorBuffer {
  case class Finished(service: Any, result: Any)
  case class ServiceResult(Status: Int, Result: Any)

  val PROCESSING = 0
  val FINISHED = 1

  val STARTED_MSG = Json.obj("status" -> "started")
  val ACK_MSG = Json.obj("status" -> "finished")
  val PROCESSING_MSG = Json.obj("status" -> "processing")
  val UNEXPECTED_STATUS_MSG = Json.obj("error" -> "Error: Unexpected status found!")
  val UNEXPECTED_FINISH_MSG = Json.obj("error" -> "Error: Unexpected finishing reported!")
}

trait ActorBuffer extends Actor {
  import context.dispatcher
  import actors.ActorBuffer._

  type BufferResult = Map[Any, ServiceResult]
  var generalBuffer: BufferResult = Map()

  var serviceWorker: ActorRef = _

  def setWorker(system: ActorSystem, workerClass: Class[_ <: akka.actor.Actor]) = {
    serviceWorker = system.actorOf(Props(workerClass), name = self.path.name + "_worker")
  }

  def getBuffer(message: Any): Option[ServiceResult] = if (generalBuffer.keys.toSet.contains(message)) Some(generalBuffer(message)) else None

  def processBufferGeneral(message: Any): Unit = {
    var result: ServiceResult = null
    message match {
      case Finished(service: Any, resultComplete: Any) if getBuffer(service).getOrElse("") == "" => Future(UNEXPECTED_FINISH_MSG) pipeTo sender
      case Finished(service: Any, resultComplete: Any) => {
        generalBuffer(service) = ServiceResult(FINISHED, resultComplete)
        Future(ACK_MSG) pipeTo sender
      }
      case _ => getBuffer(message) match {
        case Some(buffer) if buffer.Status == PROCESSING => Future(PROCESSING_MSG) pipeTo sender
        case Some(buffer) if buffer.Status == FINISHED => {
          result = generalBuffer(message)
          generalBuffer = generalBuffer - message
          Future(result.Result) pipeTo sender
        }
        case Some(buffer) => Future(UNEXPECTED_STATUS_MSG) pipeTo sender
        case None => {
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