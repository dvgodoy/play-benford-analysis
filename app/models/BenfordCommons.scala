package models

import actors.BenfordActor
import akka.actor.{Props, ActorSelection, ActorSystem}
import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import com.dvgodoy.spark.benford.util.{JobId, DataByLevel, ResultsByLevel, StatsCIByLevel}
import org.apache.spark.rdd.RDD
import play.api.libs.json._

object BenfordCommons {
  private val boot = Bootstrap()
  private val benf = Benford()

  val system = ActorSystem("Benford")

  def getJob(uuid: String): ActorSelection = {
    val path = system / uuid
    system.actorSelection(path)
  }
  def createJob: String =  {
    val uuid = java.util.UUID.randomUUID().toString
    system.actorOf(Props[BenfordActor], name = uuid)
    uuid
  }
  def loadData(filePath: String)(implicit jobId: JobId): DataByLevel = {
    SparkCommons.sc.setJobGroup(jobId.id, jobId.id)
    boot.loadData(SparkCommons.sc, filePath)(jobId)
  }
  def calcSample(numberSamples: Int = 25000, data: DataByLevel): RDD[StatsCIByLevel] = {
    boot.calcSampleCIs(SparkCommons.sc, data, numberSamples)
  }
  def calcBenford(numberSamples: Int = 25000, data: DataByLevel): RDD[StatsCIByLevel] = {
    benf.calcBenfordCIs(SparkCommons.sc, data, numberSamples)
  }
  def calcResults(sampleRDD: RDD[StatsCIByLevel], benfordRDD: RDD[StatsCIByLevel]): RDD[ResultsByLevel] = {
    boot.calcResults(sampleRDD, benfordRDD)
  }
  def getCIsByGroupId(sampleRDD: RDD[StatsCIByLevel], groupId: Int)(implicit jobId: JobId): JsValue = boot.getCIsByGroupId(sampleRDD, groupId)(jobId)
  def getCIsByLevel(sampleRDD: RDD[StatsCIByLevel], level: Int)(implicit jobId: JobId): JsValue = boot.getCIsByLevel(sampleRDD, level)(jobId)
  def getResultsByGroupId(resultsRDD: RDD[ResultsByLevel], groupId: Int)(implicit jobId: JobId): JsValue = boot.getResultsByGroupId(resultsRDD, groupId)(jobId)
  def getResultsByLevel(resultsRDD: RDD[ResultsByLevel], level: Int)(implicit jobId: JobId): JsValue = boot.getResultsByLevel(resultsRDD, level)(jobId)
  def getFrequenciesByGroupId(data: DataByLevel, groupId: Int)(implicit jobId: JobId): JsValue = boot.getFrequenciesByGroupId(data, groupId)
  def getFrequenciesByLevel(data: DataByLevel, level: Int)(implicit jobId: JobId): JsValue = boot.getFrequenciesByLevel(data, level)
  def getGroups(data: DataByLevel)(implicit jobId: JobId): JsValue = boot.getGroups(data)
}
