package actors

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.util._
import com.dvgodoy.spark.benford.constants.BenfordStatsDigits
import models.BenfordCommons
import models.BenfordService._
import org.apache.spark.rdd.RDD

import scala.concurrent.Future

class BenfordActor extends Actor with ActorLogging {

  import context.dispatcher

  override def preStart() = {
    //
  }

  private var data: DataByLevel = _
  private var numberSamples: Int = 25000

  protected var basicBoot: BasicBoot = _
  protected var dataStatsRDD: Array[RDD[((Long, Int), StatsDigits)]] = _
  protected var sampleRDD: Array[RDD[StatsCIByLevel]] = _
  protected var benfordRDD: Array[RDD[StatsCIByLevel]] = _
  protected var resultsRDD: Array[RDD[ResultsByLevel]] = _

  implicit val jobId = JobId(self.path.name)

  def calc(groupId: Int) = {
    if (dataStatsRDD(groupId) == null) {
      dataStatsRDD(groupId) = BenfordCommons.calcDataStats(data, groupId)
      sampleRDD(groupId) = BenfordCommons.calcSample(basicBoot, dataStatsRDD(groupId), data, groupId)
      benfordRDD(groupId) = BenfordCommons.calcBenford(basicBoot, dataStatsRDD(groupId), data, groupId)
      resultsRDD(groupId) = BenfordCommons.calcResults(sampleRDD(groupId), benfordRDD(groupId))
    }
  }

  def calcDataStats(groupId: Int): RDD[((Long, Int), StatsDigits)] = {
    if (dataStatsRDD(groupId) == null) {
      dataStatsRDD(groupId) = BenfordCommons.calcDataStats(data, groupId)
    }
    dataStatsRDD(groupId)
  }

  def calcSample(groupId: Int): RDD[StatsCIByLevel] = {
    if (sampleRDD(groupId) == null) {
      sampleRDD(groupId) = BenfordCommons.calcSample(basicBoot, calcDataStats(groupId), data, groupId)
    }
    sampleRDD(groupId)
  }

  def calcBenford(groupId: Int): RDD[StatsCIByLevel] = {
    if (benfordRDD(groupId) == null) {
      benfordRDD(groupId) = BenfordCommons.calcBenford(basicBoot, calcDataStats(groupId), data, groupId)
    }
    benfordRDD(groupId)
  }

  def calcResults(groupId: Int): RDD[ResultsByLevel] = {
    if (resultsRDD(groupId) == null) {
      resultsRDD(groupId) = BenfordCommons.calcResults(calcSample(groupId), calcBenford(groupId))
    }
    resultsRDD(groupId)
  }

  def receive = {
    case srvData(filePath: String) => {
      data = BenfordCommons.loadData(filePath)
      val numLevels = data.levels.keys.size
      dataStatsRDD = new Array[RDD[((Long, Int), StatsDigits)]](numLevels)
      sampleRDD = new Array[RDD[StatsCIByLevel]](numLevels)
      benfordRDD = new Array[RDD[StatsCIByLevel]](numLevels)
      resultsRDD = new Array[RDD[ResultsByLevel]](numLevels)
      sender ! Success(self.path.name)
    }
    case srvCalc(numSamples: Int) => {
      numberSamples = numSamples
      basicBoot = BenfordCommons.calcBasicBoot(data, numSamples)
      sender ! Success("")
    }
    case srvNumSamples() => {
      Future(numberSamples) pipeTo sender
    }
    case srvCIsByGroupId(groupId: Int) => {
      Future(BenfordCommons.getCIsByGroupId(calcSample(groupId))) pipeTo sender
    }
    case srvCIsByLevel(level: Int) => {
      //Future(BenfordCommons.getCIsByLevel(sampleRDD, level)) pipeTo sender
    }
    case srvBenfordCIsByGroupId(groupId: Int) => {
      calc(groupId)
      Future(BenfordCommons.getCIsByGroupId(calcBenford(groupId))) pipeTo sender
    }
    case srvBenfordCIsByLevel(level: Int) => {
      //Future(BenfordCommons.getCIsByLevel(benfordRDD, level)) pipeTo sender
    }
    case srvResultsByGroupId(groupId: Int) => {
      calc(groupId)
      Future(BenfordCommons.getResultsByGroupId(calcResults(groupId))) pipeTo sender
    }
    case srvResultsByLevel(level: Int) => {
      //Future(BenfordCommons.getResultsByLevel(resultsRDD, level)) pipeTo sender
    }
    case srvFrequenciesByGroupId(groupId: Int) => {
      Future(BenfordCommons.getFrequenciesByGroupId(data, groupId)) pipeTo sender
    }
    case srvFrequenciesByLevel(level: Int) => {
      Future(BenfordCommons.getFrequenciesByLevel(data, level)) pipeTo sender
    }
    case srvGroups() => {
      Future(BenfordCommons.getGroups(data)) pipeTo sender
    }
    case srvTestsByGroupId(groupId: Int) => {
      Future(BenfordCommons.getTestsByGroupId(data, groupId)) pipeTo sender
    }
    case srvTestsByLevel(level: Int) => {
      Future(BenfordCommons.getTestsByLevel(data, level)) pipeTo sender
    }
  }

}
