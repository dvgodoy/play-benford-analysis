package actors

import java.io.ByteArrayInputStream

import akka.actor.Status.Success
import akka.actor.{ActorLogging, Actor}
import com.dvgodoy.spark.benford.image.SBA.{SBAData, SBAImageData}
import com.dvgodoy.spark.benford.util.JobId
import models.{SparkCommons, ImageCommons}
import models.ImageService._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils

import scala.concurrent.Future

class ImageActor extends Actor with ActorLogging {

  private var image: String = _
  private var original: java.io.ByteArrayOutputStream = _
  private var data: SBAImageData = _
  private var sba: SBAData = _

  implicit val jobId = JobId(self.path.name)

  def receive = {
    case srvDirect(baos: java.io.ByteArrayOutputStream) => {
      val originalSender = sender
      original = baos
      data = ImageCommons.loadData(original)

      val is = new ByteArrayInputStream(baos.toByteArray)
      val bytes = IOUtils.toByteArray(is)
      val bytes64 = Base64.encodeBase64(bytes)
      image = new String(bytes64)

      originalSender ! Success("")
    }
    case srvData(filePath: String) => {
      val originalSender = sender
      data = ImageCommons.loadData(filePath)
      originalSender ! Success(self.path.name)
    }
    case srvCalc(windowsSize: Int) => {
      val originalSender = sender
      sba = ImageCommons.calcSBA(data, windowsSize)
      originalSender ! Success("")
    }
    case srvImage() => {
      val originalSender = sender
      originalSender ! Success(image)
    }
    case srvSBAImage(threshold: Double, whiteBackground: Boolean) => {
      val originalSender = sender
      val content = ImageCommons.getImage(sba, threshold, whiteBackground)
      originalSender ! Success(content)
    }
  }
}
