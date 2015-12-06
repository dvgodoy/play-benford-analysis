package actors

import akka.actor.Status.Success
import akka.actor.{PoisonPill, Actor, ActorLogging, Cancellable}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.util._
import models.BenfordCommons
import models.BenfordService._
import org.scalactic._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class BenfordActor extends Actor with ActorLogging {

  import context.dispatcher
  /*private val SYNC_ALL_ORDERS = "SYNC_ALL_ORDERS"
  private var scheduler: Cancellable = _

  override def preStart():Unit = {
    import scala.concurrent.duration._
    scheduler = context.system.scheduler.schedule(
      initialDelay = 10 seconds,
      interval = 15 minutes,
      receiver = self,
      message = PoisonPill
    )
  }

  override def postStop(): Unit = {
    scheduler.cancel()
  }*/

  private var data: DataByLevelMsg = _
  private var numberSamples: Int = 25000

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

  def receive = {
    // if it does any calculation, prevents the actor to receive the poison pill it sent to itself
    // scheduler.cancel()
    /*case SYNC_ALL_ORDERS =>
      try {
        // synchronize all the orders
      } catch {
        case t: Throwable =>
        // report errors
      }*/
    case srvData(filePath: String) => {
      val originalSender = sender
      data = BenfordCommons.loadData(filePath)
      val result: Or[JsValue, Every[ErrorMessage]] = data match {
        case Good(dbl) => {
          val numLevels = data.get.levels.keys.size
          dataStatsRDD = new Array[DataStatsMsg](numLevels)
          sampleRDD = new Array[StatsCIByLevelMsg](numLevels)
          benfordRDD = new Array[StatsCIByLevelMsg](numLevels)
          resultsRDD = new Array[ResultsByLevelMsg](numLevels)
          Good(Json.toJson(self.path.name))
        }
        case Bad(e) => Bad(e)
      }
      //originalSender ! Success(result)
      Future(result) pipeTo originalSender
    }
    case srvCalc(numSamples: Int) => {
      val originalSender = sender
      numberSamples = numSamples
      basicBoot = BenfordCommons.calcBasicBoot(data, numSamples)
      val result: Or[JsValue, Every[ErrorMessage]] = basicBoot match {
        case Good(bb) => Good(Json.toJson("ok"))
        case Bad(e) => Bad(e)
      }
      //originalSender ! Success(result)
      Future(result) pipeTo originalSender
    }
    case srvNumSamples() => {
      val originalSender = sender
      Future(numberSamples) pipeTo originalSender
    }
    case srvCIsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getCIsByGroupId(calcSample(groupId))) pipeTo originalSender
    }
    case srvCIsByLevel(level: Int) => {
      // to be implemented
    }
    case srvBenfordCIsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getCIsByGroupId(calcBenford(groupId))) pipeTo originalSender
    }
    case srvBenfordCIsByLevel(level: Int) => {
      // to be implemented
    }
    case srvResultsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getResultsByGroupId(calcResults(groupId))) pipeTo originalSender
    }
    case srvResultsByLevel(level: Int) => {
      // to be implemented
    }
    case srvFrequenciesByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getFrequenciesByGroupId(data, groupId)) pipeTo originalSender
    }
    case srvFrequenciesByLevel(level: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getFrequenciesByLevel(data, level)) pipeTo originalSender
    }
    case srvGroups() => {
      val originalSender = sender
      Future(BenfordCommons.getGroups(data)) pipeTo originalSender
    }
    case srvTestsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getTestsByGroupId(data, groupId)) pipeTo originalSender
    }
    case srvTestsByLevel(level: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getTestsByLevel(data, level)) pipeTo originalSender
    }
  }

}
