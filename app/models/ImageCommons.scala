package models

import actors.ImageBufferActor
import akka.actor.{Props, ActorSelection, ActorSystem}
import com.dvgodoy.spark.benford.util.JobId
import com.dvgodoy.spark.benford.image.SBA._

object ImageCommons {
  val system = ActorSystem("SBA")

  def getJob(uuid: String): ActorSelection = {
    val path = system / uuid
    system.actorSelection(path)
  }
  def createJob: String =  {
    val uuid = java.util.UUID.randomUUID().toString
    system.actorOf(Props[ImageBufferActor], name = uuid)
    uuid
  }
  def loadData(baos: java.io.ByteArrayOutputStream): SBAImageDataMsg = {
    loadDirect(baos)
  }
  def loadData(filePath: String): SBAImageDataMsg = {
    loadImage(filePath)
  }
  def calcSBA(imageData: SBAImageDataMsg, wSize: Int = 15)(implicit jobId: JobId): SBADataMsg = {
    SparkCommons.sc.setJobGroup(jobId.id, jobId.id)
    performSBA(SparkCommons.sc, imageData, wSize)(jobId)
  }
  def getImage(sbaData: SBADataMsg, threshold: Double, whiteBackground: Boolean = true): SBAEncodedMsg = {
    getSBAImage(sbaData, threshold, whiteBackground)
  }
}
