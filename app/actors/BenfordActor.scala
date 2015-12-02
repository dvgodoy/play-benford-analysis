package actors

import akka.actor.Status.Success
import akka.actor.{PoisonPill, Actor, ActorLogging, Cancellable}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.util._
import models.BenfordCommons
import models.BenfordService._
import org.apache.spark.rdd.RDD

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

  private var data: DataByLevel = _
  private var numberSamples: Int = 25000

  protected var basicBoot: BasicBoot = _
  protected var dataStatsRDD: Array[RDD[((Long, Int), StatsDigits)]] = _
  protected var sampleRDD: Array[RDD[StatsCIByLevel]] = _
  protected var benfordRDD: Array[RDD[StatsCIByLevel]] = _
  protected var resultsRDD: Array[RDD[ResultsByLevel]] = _

  implicit val jobId = JobId(self.path.name)

  /*def calc(groupId: Int) = {
    if (dataStatsRDD(groupId) == null) {
      dataStatsRDD(groupId) = BenfordCommons.calcDataStats(data, groupId)
      sampleRDD(groupId) = BenfordCommons.calcSample(basicBoot, dataStatsRDD(groupId), data, groupId)
      benfordRDD(groupId) = BenfordCommons.calcBenford(basicBoot, dataStatsRDD(groupId), data, groupId)
      resultsRDD(groupId) = BenfordCommons.calcResults(sampleRDD(groupId), benfordRDD(groupId))
    }
  }*/

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
      val numLevels = data.levels.keys.size
      dataStatsRDD = new Array[RDD[((Long, Int), StatsDigits)]](numLevels)
      sampleRDD = new Array[RDD[StatsCIByLevel]](numLevels)
      benfordRDD = new Array[RDD[StatsCIByLevel]](numLevels)
      resultsRDD = new Array[RDD[ResultsByLevel]](numLevels)
      originalSender ! Success(self.path.name)
    }
    case srvCalc(numSamples: Int) => {
      val originalSender = sender
      numberSamples = numSamples
      basicBoot = BenfordCommons.calcBasicBoot(data, numSamples)
      originalSender ! Success("")
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
      //Future(BenfordCommons.getCIsByLevel(sampleRDD, level)) pipeTo sender
    }
    case srvBenfordCIsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getCIsByGroupId(calcBenford(groupId))) pipeTo originalSender
    }
    case srvBenfordCIsByLevel(level: Int) => {
      //Future(BenfordCommons.getCIsByLevel(benfordRDD, level)) pipeTo sender
    }
    case srvResultsByGroupId(groupId: Int) => {
      val originalSender = sender
      Future(BenfordCommons.getResultsByGroupId(calcResults(groupId))) pipeTo originalSender
    }
    case srvResultsByLevel(level: Int) => {
      //Future(BenfordCommons.getResultsByLevel(resultsRDD, level)) pipeTo sender
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
