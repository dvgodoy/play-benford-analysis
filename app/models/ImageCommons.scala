package models

import actors.ImageActor
import akka.actor.{Props, ActorSelection, ActorSystem}
import com.dvgodoy.spark.benford.util.JobId
import com.dvgodoy.spark.benford.image.SBA._

import org.scalactic._

object ImageCommons {
  val system = ActorSystem("SBA")

  def getJob(uuid: String): ActorSelection = {
    val path = system / uuid
    system.actorSelection(path)
  }
  def createJob: String =  {
    val uuid = java.util.UUID.randomUUID().toString
    system.actorOf(Props[ImageActor], name = uuid)
    uuid
  }
  def loadData(baos: java.io.ByteArrayOutputStream): Or[SBAImageData, One[ErrorMessage]] = {
    loadDirect(baos)
  }
  def loadData(filePath: String) = {
    loadImage(filePath)
  }
  def calcSBA(imageData: SBAImageData, wSize: Int = 15)(implicit jobId: JobId): SBAData = {
    SparkCommons.sc.setJobGroup(jobId.id, jobId.id)
    performSBA(SparkCommons.sc, imageData, wSize)(jobId)
  }
  def getImage(sbaData: SBAData, threshold: Double, whiteBackground: Boolean = true): String = {
    getSBAImage(sbaData, threshold, whiteBackground)
  }
}
