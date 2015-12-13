package controllers

import java.util.concurrent.TimeUnit._

import actors.ActorBuffer.Finished
import akka.pattern.ask
import akka.util.Timeout
import models.{SparkCommons, BenfordCommons}
import models.BenfordService._
import play.api.libs.json.{JsUndefined, JsDefined, JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future
import sys.process._
import java.net.URL
import java.io.File

class BenfordBootstrap extends Controller {
  implicit val timeout = Timeout(30, SECONDS)

  def fileDownloader(url: String, filename: String) = {
    new URL(url) #> new File(filename) !!
  }

  def processResult(result: Finished, error: Status, session: Session): Result = {
    processResult(result.result.asInstanceOf[JsValue], error, session)
  }

  def processResult(result: JsValue, error: Status, session: Session): Result = {
    (result \ "error") match {
      case msg: JsDefined => error(result).withSession(session)
      case res: JsUndefined => Ok(result).withSession(session)
    }
  }

  def askActor(id: String, buffer: Boolean, session: Session, message: scala.Any, error: Status): Future[Result] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id + (if (!buffer) "_worker" else ""))
    val res: Future[Result] = if (!buffer) {
      for {
        bnf <- ask(benfordActor, message).mapTo[Finished]
      } yield processResult(bnf, error, session + ("job", id))
    } else {
      for {
        bnf <- ask(benfordActor, message).mapTo[JsValue]
      } yield processResult(bnf, error, session + ("job", id))
    }
    res
  }

  def accUpload = Action.async(parse.multipartFormData) { request =>
    import scala.concurrent.ExecutionContext.Implicits.global

    val id = BenfordCommons.createJob
    val filePath = SparkCommons.tmpFolder + "/" + id + ".csv"
    val res = try {
      val accData = request.body.files(0)
      accData.ref.moveTo(new File(filePath))
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".csv")
      Json.obj("job" -> Json.toJson(id))
    } catch {
      case ex: Exception => Json.obj("error" -> "Error: There was a problem uploading your file. Please try again.")
    }
    Future(processResult(res, NotFound, request.session + ("job", id) + ("filePath", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath)))
  }

  def loadDataURL = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global

    val id = BenfordCommons.createJob
    val filePath = SparkCommons.tmpFolder + "/" + id + ".csv"
    val url = {
      try {
        request.body.asMultipartFormData.get.dataParts.get("accURL").get.head
      } catch {
        case ex: Exception => request.body.asFormUrlEncoded.get("accURL").head
      }
    }
    val res = try {
      fileDownloader(url, filePath)
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".csv")
      Json.obj("job" -> Json.toJson(id))
    } catch {
      case ex: Exception => Json.obj("error" -> "Error: There was a problem uploading your file. Please try again.")
    }
    Future(processResult(res, NotFound, request.session + ("job", id) + ("filePath", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath)))
  }

  def loadDataSession(async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    val filePath = request.session.get("filePath").getOrElse("")
    loadData(id, filePath, async).apply(request)
  }

  def loadDataUploaded(id: String, async: Boolean) = Action.async { request =>
    val filePath = if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + SparkCommons.tmpFolder + "/" + id + ".csv" else "file://" + SparkCommons.tmpFolder + "/" + id + ".csv"
    loadData(id, filePath, async).apply(request)
  }

  def loadDataLocal(filePath: String, async: Boolean) = Action.async { request =>
    val id = BenfordCommons.createJob
    loadData(id, filePath, async).apply(request)
  }

  def loadData(id: String, filePath: String, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvData(filePath), NotAcceptable)
  }

  def getGroupsSession(async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getGroups(id, async).apply(request)
  }

  def getGroups(id: String, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvGroups(), BadRequest)
  }

  def calculateSession(numSamples: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    calculate(id, numSamples, async).apply(request)
  }

  def calculate(id: String, numSamples: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvCalc(numSamples), BadRequest)
  }

  def getCIsByGroupSession(groupId: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getCIsByGroup(id, groupId, async).apply(request)
  }

  def getCIsByGroup(id: String, groupId: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvCIsByGroupId(groupId), BadRequest)
  }

  def getBenfordCIsByGroupSession(groupId: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getBenfordCIsByGroup(id, groupId, async).apply(request)
  }

  def getBenfordCIsByGroup(id: String, groupId: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvBenfordCIsByGroupId(groupId), BadRequest)
  }

  def getResultsByGroupSession(groupId: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getResultsByGroup(id, groupId, async).apply(request)
  }

  def getResultsByGroup(id: String, groupId: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvResultsByGroupId(groupId), BadRequest)
  }

  def getFreqByGroupSession(groupId: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getFreqByGroup(id, groupId, async).apply(request)
  }

  def getFreqByGroup(id: String, groupId: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvFrequenciesByGroupId(groupId), BadRequest)
  }

  def getExactBenfordParams = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      p <- Future(BenfordCommons.getExactBenfordParams)
    } yield processResult(p, BadRequest, request.session)
    res
  }

  def getExactBenfordProbs = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      p <- Future(BenfordCommons.getExactBenfordProbs)
    } yield processResult(p, BadRequest, request.session)
    res
  }

  def getTestsByGroupSession(groupId: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getTestsByGroup(id, groupId, async).apply(request)
  }

  def getTestsByGroup(id: String, groupId: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvTestsByGroupId(groupId), BadRequest)
  }

}
