package actors

import actors.ActorBuffer.Finished
import akka.ActorTimer
import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.util._
import models.BenfordCommons
import models.BenfordService._
import org.scalactic._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class BenfordActor extends Actor with ActorLogging with ActorTimer {

  import context.dispatcher

  private var data: DataByLevelMsg = _
  private var numberSamples: Int = 0

  protected var basicBoot: BasicBootMsg = _
  protected var dataStatsRDD: Array[DataStatsMsg] = _
  protected var sampleRDD: Array[StatsCIByLevelMsg] = _
  protected var benfordRDD: Array[StatsCIByLevelMsg] = _
  protected var resultsRDD: Array[ResultsByLevelMsg] = _

  implicit val jobId = JobId(self.path.name)

  def calcDataStats(groupId: Int): DataStatsMsg = {
    if (dataStatsRDD(groupId) == null) {
      dataStatsRDD(groupId) = BenfordCommons.calcDataStats(data, groupId)
    }
    dataStatsRDD(groupId)
  }

  def calcSample(groupId: Int): StatsCIByLevelMsg = {
    if (sampleRDD(groupId) == null) {
      sampleRDD(groupId) = BenfordCommons.calcSample(basicBoot, calcDataStats(groupId), data, groupId)
    }
    sampleRDD(groupId)
  }

  def calcBenford(groupId: Int): StatsCIByLevelMsg = {
    if (benfordRDD(groupId) == null) {
      benfordRDD(groupId) = BenfordCommons.calcBenford(basicBoot, calcDataStats(groupId), data, groupId)
    }
    benfordRDD(groupId)
  }

  def calcResults(groupId: Int): ResultsByLevelMsg = {
    if (resultsRDD(groupId) == null) {
      resultsRDD(groupId) = BenfordCommons.calcResults(calcSample(groupId), calcBenford(groupId))
    }
    resultsRDD(groupId)
  }

  def init: Unit = {
    val numLevels = data.get.levels.keys.size
    dataStatsRDD = new Array[DataStatsMsg](numLevels)
    sampleRDD = new Array[StatsCIByLevelMsg](numLevels)
    benfordRDD = new Array[StatsCIByLevelMsg](numLevels)
    resultsRDD = new Array[ResultsByLevelMsg](numLevels)
  }

  def receive = {
    case srvData(filePath: String) => {
      val originalSender = sender
      data = BenfordCommons.loadData(filePath)
      val result: JsValue = data match {
        case Good(dbl) => {
          init
          Json.obj("job" -> Json.toJson(self.path.name.slice(0,self.path.name.length - 7)))
        }
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvData(filePath), _)) pipeTo originalSender
    }
    case srvCalc(numSamples: Int) => {
      val originalSender = sender
      if (numSamples != numberSamples) init
      numberSamples = numSamples
      basicBoot = BenfordCommons.calcBasicBoot(data, numSamples)
      val result: JsValue = basicBoot match {
        case Good(s) => Json.obj("calc" -> "ok")
        case Bad(e) => Json.obj("error" -> Json.toJson(e.head))
      }
      Future(result) map (Finished(srvCalc(numSamples), _)) pipeTo originalSender
    }
    case srvNumSamples() => {
      val originalSender = sender
      Future(numberSamples)  map (Finished(srvNumSamples(), _)) pipeTo originalSender
    }
    case srvCIsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getCIsByGroupId(calcSample(groupId))) map (Finished(srvCIsByGroupId(groupId), _)) pipeTo originalSender
    }
    case srvBenfordCIsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getCIsByGroupId(calcBenford(groupId))) map (Finished(srvBenfordCIsByGroupId(groupId), _)) pipeTo originalSender
    }
    case srvResultsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getResultsByGroupId(calcResults(groupId))) map (Finished(srvResultsByGroupId(groupId), _)) pipeTo originalSender
    }
    case srvFrequenciesByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getFrequenciesByGroupId(data, groupId)) map (Finished(srvFrequenciesByGroupId(groupId), _)) pipeTo originalSender
    }
    case srvGroups() => {
      val originalSender = sender
      Future(BenfordCommons.getGroups(data)) map (Finished(srvGroups(), _)) pipeTo originalSender
    }
    case srvTestsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getTestsByGroupId(data, groupId)) map (Finished(srvTestsByGroupId(groupId), _)) pipeTo originalSender
    }
  }

}
