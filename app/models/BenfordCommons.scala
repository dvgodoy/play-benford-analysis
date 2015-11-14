package models

import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import com.dvgodoy.spark.benford.util.{DataByLevel, ResultsByLevel, StatsCIByLevel}
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
  def loadData(filePath: String): Unit = data = boot.loadData(SparkCommons.sc, filePath)
  def calculate: Unit = { calcSample; calcBenford; calcResults }
  def getCIsByGroupId(groupId: Int): JsValue = boot.getCIsByGroupId(sampleRDD, groupId)
  def getCIsByLevel(level: Int): JsValue = boot.getCIsByLevel(sampleRDD, level)
  def getBenfordCIsByGroupId(groupId: Int): JsValue = boot.getCIsByGroupId(benfordRDD, groupId)
  def getBenfordCIsByLevel(level: Int): JsValue = boot.getCIsByLevel(benfordRDD, level)
  def getResultsByGroupId(groupId: Int): JsValue = boot.getResultsByGroupId(resultsRDD, groupId)
  def getResultsByLevel(level: Int): JsValue = boot.getResultsByLevel(resultsRDD, level)
  def getFrequenciesByGroupId(groupId: Int): JsValue = boot.getFrequenciesByGroupId(data, groupId)
  def getFrequenciesByLevel(level: Int): JsValue = boot.getFrequenciesByLevel(data, level)
  def getGroups: JsValue = boot.getGroups(data)
}
