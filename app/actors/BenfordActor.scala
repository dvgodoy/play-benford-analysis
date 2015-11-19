package actors

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.dvgodoy.spark.benford.util.{DataByLevel, JobId, ResultsByLevel, StatsCIByLevel}
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
  private var sampleRDD: RDD[StatsCIByLevel] = _
  private var benfordRDD: RDD[StatsCIByLevel] = _
  private var resultsRDD: RDD[ResultsByLevel] = _
  private var numberSamples: Int = 25000

  implicit val jobId = JobId(self.path.name)

  def receive = {
    case srvData(filePath: String) => {
      data = BenfordCommons.loadData(filePath)
      sender ! Success(self.path.name)
    }
    case srvCalc(numSamples: Int) => {
      numberSamples = numSamples
      sampleRDD = BenfordCommons.calcSample(numberSamples, data)
      benfordRDD = BenfordCommons.calcBenford(numberSamples, data)
      resultsRDD = BenfordCommons.calcResults(sampleRDD, benfordRDD)
      sender ! Success("")
    }
    case srvNumSamples() => {
      Future(numberSamples) pipeTo sender
    }
    case srvCIsByGroupId(groupId: Int) => {
      Future(BenfordCommons.getCIsByGroupId(sampleRDD, groupId)) pipeTo sender
    }
    case srvCIsByLevel(level: Int) => {
      Future(BenfordCommons.getCIsByLevel(sampleRDD, level)) pipeTo sender
    }
    case srvBenfordCIsByGroupId(groupId: Int) => {
      Future(BenfordCommons.getCIsByGroupId(benfordRDD, groupId)) pipeTo sender
    }
    case srvBenfordCIsByLevel(level: Int) => {
      Future(BenfordCommons.getCIsByLevel(benfordRDD, level)) pipeTo sender
    }
    case srvResultsByGroupId(groupId: Int) => {
      Future(BenfordCommons.getResultsByGroupId(resultsRDD, groupId)) pipeTo sender
    }
    case srvResultsByLevel(level: Int) => {
      Future(BenfordCommons.getResultsByLevel(resultsRDD, level)) pipeTo sender
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
  }

}
