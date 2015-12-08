package models

import actors.BenfordBufferActor
import akka.actor.{Props, ActorSelection, ActorSystem}
import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import com.dvgodoy.spark.benford.util._
import play.api.libs.json._

object BenfordCommons {
  private val boot = Bootstrap()
  private val benf = Benford()
  private val exactParams = benf.getExactBenfordParams
  private val exactProbs = benf.getExactBenfordProbs

  val system = ActorSystem("Benford")

  def getJob(uuid: String): ActorSelection = {
    val path = system / uuid
    system.actorSelection(path)
  }
  def createJob: String =  {
    val uuid = java.util.UUID.randomUUID().toString
    system.actorOf(Props[BenfordBufferActor], name = uuid)
    uuid
  }
  def loadData(filePath: String)(implicit jobId: JobId): DataByLevelMsg = {
    SparkCommons.sc.setJobGroup(jobId.id, jobId.id)
    boot.loadData(SparkCommons.sc, filePath)(jobId)
  }
  def calcBasicBoot(data: DataByLevelMsg, numberSamples: Int = 25000): BasicBootMsg = {
    boot.calcBasicBoot(SparkCommons.sc, data, numberSamples)
  }
  def calcDataStats(data: DataByLevelMsg, groupId: Int): DataStatsMsg = {
    boot.calcDataStats(data, groupId)
  }
  def calcSample(basicBoot: BasicBootMsg, dataStatsRDD: DataStatsMsg, data: DataByLevelMsg, groupId: Int): StatsCIByLevelMsg = {
    boot.calcSampleCIs(basicBoot, dataStatsRDD, data, groupId)
  }
  def calcBenford(basicBoot: BasicBootMsg, dataStatsRDD: DataStatsMsg, data: DataByLevelMsg, groupId: Int): StatsCIByLevelMsg = {
    benf.calcBenfordCIs(basicBoot, dataStatsRDD, data, groupId)
  }
  def calcResults(sampleRDD: StatsCIByLevelMsg, benfordRDD: StatsCIByLevelMsg): ResultsByLevelMsg = {
    boot.calcResults(sampleRDD, benfordRDD)
  }
  def getCIsByGroupId(sampleRDD: StatsCIByLevelMsg)(implicit jobId: JobId): JsValue = boot.getCIs(sampleRDD)(jobId)
  def getResultsByGroupId(resultsRDD: ResultsByLevelMsg)(implicit jobId: JobId): JsValue = boot.getResults(resultsRDD)(jobId)
  def getFrequenciesByGroupId(data: DataByLevelMsg, groupId: Int): JsValue = boot.getFrequenciesByGroupId(data, groupId)
  def getFrequenciesByLevel(data: DataByLevelMsg, level: Int): JsValue = boot.getFrequenciesByLevel(data, level)
  def getGroups(data: DataByLevelMsg): JsValue = boot.getGroups(data)
  def getExactBenfordParams: JsValue = exactParams
  def getExactBenfordProbs: JsValue = exactProbs
  def getTestsByGroupId(data: DataByLevelMsg, groupId: Int): JsValue = boot.getTestsByGroupId(data, groupId)
  def getTestsByLevel(data: DataByLevelMsg, level: Int): JsValue = boot.getTestsByLevel(data, level)
}
