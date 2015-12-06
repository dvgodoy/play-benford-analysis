package actors

import java.io.ByteArrayInputStream

import akka.actor.{ActorLogging, Actor}
import com.dvgodoy.spark.benford.image.SBA.{SBAData, SBAImageData}
import com.dvgodoy.spark.benford.util.JobId
import models.ImageCommons
import models.ImageService._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.scalactic._
import play.api.libs.json.{JsValue, Json}

import scala.util.Failure

class ImageActor extends Actor with ActorLogging {

  private var image: String = _
  private var original: java.io.ByteArrayOutputStream = _
  private var data: Or[SBAImageData, One[ErrorMessage]] = _
  private var sba: SBAData = _

  implicit val jobId = JobId(self.path.name)

  def receive = {
    case srvDirect(baos: java.io.ByteArrayOutputStream) => {
      val originalSender = sender
      data = ImageCommons.loadData(baos)
      val result: Or[JsValue, Every[ErrorMessage]] = data match {
        case Good(s) => {
          original = baos
          val is = new ByteArrayInputStream(baos.toByteArray)
          val bytes = IOUtils.toByteArray(is)
          val bytes64 = Base64.encodeBase64(bytes)
          image = new String(bytes64)
          Good(Json.toJson(self.path.name))
        }
        case Bad(e) => Bad(e)
      }
      originalSender ! akka.actor.Status.Success(result)
    }
    case srvData(filePath: String) => {
      val originalSender = sender
      data = Good(ImageCommons.loadData(filePath))
      originalSender ! akka.actor.Status.Success(self.path.name)
    }
    case srvCalc(windowsSize: Int) => {
      val originalSender = sender
      sba = ImageCommons.calcSBA(data.get, windowsSize)
      originalSender ! akka.actor.Status.Success("")
    }
    case srvImage() => {
      val originalSender = sender
      originalSender ! akka.actor.Status.Success(image)
    }
    case srvSBAImage(threshold: Double, whiteBackground: Boolean) => {
      val originalSender = sender
      val content = ImageCommons.getImage(sba, threshold, whiteBackground)
      originalSender ! akka.actor.Status.Success(content)
    }
  }
}
