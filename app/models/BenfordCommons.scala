package models

import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import com.dvgodoy.spark.benford.util.{JobId, DataByLevel, ResultsByLevel, StatsCIByLevel}
import org.apache.spark.rdd.RDD
import play.api.libs.json._

object BenfordCommons {
  private val boot = Bootstrap()
  private val benf = Benford()
  private var data: DataByLevel = _
  private var sampleRDD: RDD[StatsCIByLevel] = _
  private var benfordRDD: RDD[StatsCIByLevel] = _
  private var resultsRDD: RDD[ResultsByLevel] = _
  private var numberSamples: Int = 25000

  private def calcSample: Unit = { sampleRDD = boot.calcSampleCIs(SparkCommons.sc, data, numberSamples); sampleRDD.cache() }
  private def calcBenford: Unit = { benfordRDD = benf.calcBenfordCIs(SparkCommons.sc, data, numberSamples); benfordRDD.cache() }
  private def calcResults: Unit = { resultsRDD = boot.calcResults(sampleRDD, benfordRDD); resultsRDD.cache() }

  def setNumberSamples(numSamples: Int): Unit = numberSamples = numSamples
  def getNumberSamples: Int = numberSamples
  def loadData(filePath: String, jobId: JobId): Unit = { SparkCommons.sc.setJobGroup(jobId.id, jobId.id); data = boot.loadData(SparkCommons.sc, filePath)(jobId) }
  def calculate(numSamples: Int = 25000): Unit = { setNumberSamples(numSamples); calcSample; calcBenford; calcResults }
  def getCIsByGroupId(groupId: Int, jobId: JobId): JsValue = boot.getCIsByGroupId(sampleRDD, groupId)(jobId)
  def getCIsByLevel(level: Int, jobId: JobId): JsValue = boot.getCIsByLevel(sampleRDD, level)(jobId)
  def getBenfordCIsByGroupId(groupId: Int, jobId: JobId): JsValue = boot.getCIsByGroupId(benfordRDD, groupId)(jobId)
  def getBenfordCIsByLevel(level: Int, jobId: JobId): JsValue = boot.getCIsByLevel(benfordRDD, level)(jobId)
  def getResultsByGroupId(groupId: Int, jobId: JobId): JsValue = boot.getResultsByGroupId(resultsRDD, groupId)(jobId)
  def getResultsByLevel(level: Int, jobId: JobId): JsValue = boot.getResultsByLevel(resultsRDD, level)(jobId)
  def getFrequenciesByGroupId(groupId: Int, jobId: JobId): JsValue = boot.getFrequenciesByGroupId(data, groupId)
  def getFrequenciesByLevel(level: Int, jobId: JobId): JsValue = boot.getFrequenciesByLevel(data, level)
  def getGroups(jobId: JobId): JsValue = boot.getGroups(data)
}
