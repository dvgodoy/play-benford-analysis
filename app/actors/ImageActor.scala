package actors

import akka.ActorTimer
import akka.actor.{ActorLogging, Actor}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.image.SBA._
import com.dvgodoy.spark.benford.util.JobId
import models.ImageCommons
import models.ImageService._
import org.scalactic._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

import actors.ActorBuffer.Finished

class ImageActor extends Actor with ActorLogging with ActorTimer {

  import context.dispatcher

  private var data: SBAImageDataMsg = _
  private var sba: SBADataMsg = _

  implicit val jobId = JobId(self.path.name)

  override def receive = {
    case srvDirect(baos: java.io.ByteArrayOutputStream) => {
      val originalSender = sender
      data = ImageCommons.loadData(baos)
      val result: JsValue = data match {
        case Good(s) => {
          Json.obj("job" -> Json.toJson(self.path.name.slice(0,self.path.name.length - 7)))
        }
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvDirect(baos), _)) pipeTo originalSender
    }
    case srvData(filePath: String) => {
      val originalSender = sender
      data = ImageCommons.loadData(filePath)
      val result: JsValue = data match {
        case Good(s) => {
          Json.obj("job" -> Json.toJson(self.path.name.slice(0,self.path.name.length - 7)))
        }
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvData(filePath), _)) pipeTo originalSender
    }
    case srvCalc(windowSize: Int) => {
      val originalSender = sender
      sba = ImageCommons.calcSBA(data, windowSize)
      val result: JsValue = sba match {
        case Good(s) => Json.obj("calc" -> "ok")
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvCalc(windowSize), _)) pipeTo originalSender
    }
    case srvImage() => {
      val originalSender = sender
      val result: JsValue = if (data.get.originalImage == null) {
        Json.obj("error" -> "Error: Cannot load original image.")
      } else {
        Json.obj("image" -> Json.toJson(data.get.originalImage))
      }
      Future(result) map (Finished(srvImage(), _)) pipeTo originalSender
    }
    case srvSBAImage(threshold: Double, whiteBackground: Boolean) => {
      val originalSender = sender
      val content = ImageCommons.getImage(sba, threshold, whiteBackground)
      val result: JsValue = content match {
        case Good(image) => Json.obj("image" -> Json.toJson(image))
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvSBAImage(threshold, whiteBackground), _)) pipeTo originalSender
    }
  }
}
