package models

import actors.BenfordActor
import akka.actor.{Props, ActorSelection, ActorSystem}
import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import com.dvgodoy.spark.benford.util._
import org.apache.spark.rdd.RDD
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
    system.actorOf(Props[BenfordActor], name = uuid)
    uuid
  }
  def loadData(filePath: String)(implicit jobId: JobId): DataByLevel = {
    SparkCommons.sc.setJobGroup(jobId.id, jobId.id)
    boot.loadData(SparkCommons.sc, filePath)(jobId)
  }
  def calcBasicBoot(data: DataByLevel, numberSamples: Int = 25000): BasicBoot = {
    boot.calcBasicBoot(SparkCommons.sc, data, numberSamples)
  }
  def calcDataStats(data: DataByLevel, groupId: Int): RDD[((Long, Int), StatsDigits)] = {
    boot.calcDataStats(data, groupId)
  }
  def calcSample(basicBoot: BasicBoot, dataStatsRDD: RDD[((Long, Int), StatsDigits)], data: DataByLevel, groupId: Int): RDD[StatsCIByLevel] = {
    boot.calcSampleCIs(basicBoot, dataStatsRDD, data, groupId)
  }
  def calcBenford(basicBoot: BasicBoot, dataStatsRDD: RDD[((Long, Int), StatsDigits)], data: DataByLevel, groupId: Int): RDD[StatsCIByLevel] = {
    benf.calcBenfordCIs(basicBoot, dataStatsRDD, data, groupId)
  }
  def calcResults(sampleRDD: RDD[StatsCIByLevel], benfordRDD: RDD[StatsCIByLevel]): RDD[ResultsByLevel] = {
    boot.calcResults(sampleRDD, benfordRDD)
  }
  def getCIsByGroupId(sampleRDD: RDD[StatsCIByLevel])(implicit jobId: JobId): JsValue = boot.getCIs(sampleRDD)(jobId)
  //def getCIsByLevel(sampleRDD: RDD[StatsCIByLevel], level: Int)(implicit jobId: JobId): JsValue = boot.getCIsByLevel(sampleRDD, level)(jobId)
  def getResultsByGroupId(resultsRDD: RDD[ResultsByLevel])(implicit jobId: JobId): JsValue = boot.getResults(resultsRDD)(jobId)
  //def getResultsByLevel(resultsRDD: RDD[ResultsByLevel], level: Int)(implicit jobId: JobId): JsValue = boot.getResultsByLevel(resultsRDD, level)(jobId)
  def getFrequenciesByGroupId(data: DataByLevel, groupId: Int): JsValue = boot.getFrequenciesByGroupId(data, groupId)
  def getFrequenciesByLevel(data: DataByLevel, level: Int): JsValue = boot.getFrequenciesByLevel(data, level)
  def getGroups(data: DataByLevel): JsValue = boot.getGroups(data)
  def getExactBenfordParams: JsValue = exactParams
  def getExactBenfordProbs: JsValue = exactProbs
  def getTestsByGroupId(data: DataByLevel, groupId: Int): JsValue = boot.getTestsByGroupId(data, groupId)
  def getTestsByLevel(data: DataByLevel, level: Int): JsValue = boot.getTestsByLevel(data, level)
}
