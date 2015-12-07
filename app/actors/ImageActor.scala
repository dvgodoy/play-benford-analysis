package actors

import java.io.ByteArrayInputStream

import akka.actor.{ActorLogging, Actor}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.image.SBA._
import com.dvgodoy.spark.benford.util.JobId
import models.ImageCommons
import models.ImageService._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.scalactic._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class ImageActor extends Actor with ActorLogging {

  import context.dispatcher

  private var image: String = _
  private var original: java.io.ByteArrayOutputStream = _
  private var data: SBAImageDataMsg = _
  private var sba: SBADataMsg = _

  implicit val jobId = JobId(self.path.name)

  def receive = {
    case srvDirect(baos: java.io.ByteArrayOutputStream) => {
      val originalSender = sender
      data = ImageCommons.loadData(baos)
      val result: JsValue = data match {
        case Good(s) => {
          original = baos
          val is = new ByteArrayInputStream(baos.toByteArray)
          val bytes = IOUtils.toByteArray(is)
          val bytes64 = Base64.encodeBase64(bytes)
          image = new String(bytes64)
          Json.obj("job" -> Json.toJson(self.path.name))
        }
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) pipeTo originalSender
    }
    case srvData(filePath: String) => {
      val originalSender = sender
      data = ImageCommons.loadData(filePath)
      val result: JsValue = data match {
        case Good(s) => Json.obj("job" -> Json.toJson(self.path.name))
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) pipeTo originalSender
    }
    case srvCalc(windowsSize: Int) => {
      val originalSender = sender
      sba = ImageCommons.calcSBA(data, windowsSize)
      val result: JsValue = sba match {
        case Good(s) => Json.toJson("")
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) pipeTo originalSender
    }
    case srvImage() => {
      val originalSender = sender
      val result: JsValue = if (image.length == 0) {
        Json.obj("error" -> "Error: Cannot load original image.")
      } else {
        Json.obj("image" -> Json.toJson(image))
      }
      Future(result) pipeTo originalSender
    }
    case srvSBAImage(threshold: Double, whiteBackground: Boolean) => {
      val originalSender = sender
      val content = ImageCommons.getImage(sba, threshold, whiteBackground)
      val result: JsValue = content match {
        case Good(image) => Json.obj("image" -> Json.toJson(image))
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) pipeTo originalSender
    }
  }
}
