package controllers

import java.util.concurrent.TimeUnit._

import akka.pattern.ask
import akka.util.Timeout
import java.io.File
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

  def processResult(result: JsValue, error: Status, session: Session): Result = {
    (result \ "error") match {
      //case msg: JsDefined => error(msg.get).withSession(session)
      case msg: JsDefined => error(result).withSession(session)
      case res: JsUndefined => Ok(result).withSession(session)
    }
  }

  def accUpload = Action(parse.multipartFormData) { request =>
    val id = BenfordCommons.createJob
    request.body.file("accData").map { accData =>
      val filePath = SparkCommons.tmpFolder + "/" + id + ".csv"
      accData.ref.moveTo(new File(filePath))
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".csv")
      Ok("").withSession(request.session + ("job", id) + ("filePath", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath))
    }.getOrElse {
      NotFound(Json.obj("error" -> "Error: There was a problem uploading your file. Please try again."))
    }
  }

  def loadDataURL = Action(parse.multipartFormData) { request =>
    val id = BenfordCommons.createJob
    request.body.dataParts.get("accURL").map { accData =>
      val url = accData.head
      val filePath = SparkCommons.tmpFolder + "/" + id + ".csv"
      fileDownloader(url, filePath)
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".csv")
      Ok("").withSession(request.session + ("job", id) + ("filePath", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath))
    }.getOrElse{
      NotFound(Json.obj("error" -> "Error: There was a problem uploading your file. Please try again."))
    }
  }

  def loadDataSession = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    val filePath = request.session.get("filePath").getOrElse("")
    loadData(id, filePath).apply(request)
  }

  def loadDataUploaded(id: String) = Action.async { request =>
    val filePath = if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + SparkCommons.tmpFolder + "/" + id + ".csv" else "file://" + SparkCommons.tmpFolder + "/" + id + ".csv"
    loadData(id, filePath).apply(request)
  }

  def loadDataLocal(filePath: String) = Action.async { request =>
    val id = BenfordCommons.createJob
    loadData(id, filePath).apply(request)
  }

  def loadData(id: String, filePath: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      rsp <- ask(benfordActor, srvData(filePath)).mapTo[JsValue]
    } yield processResult(rsp, NotAcceptable, request.session + ("job", id))
    res
  }

  def getGroupsSession = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getGroups(id).apply(request)
  }

  def getGroups(id: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvGroups()).mapTo[JsValue]
    } yield processResult(f, BadRequest, request.session)
    res
  }

  def calculateSession(numSamples: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    calculate(id, numSamples).apply(request)
  }

  def calculate(id: String, numSamples: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      rsp <- ask(benfordActor, srvCalc(numSamples)).mapTo[JsValue]
    } yield processResult(rsp, BadRequest, request.session)
    res
  }

  def getCIsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getCIsByGroup(id, groupId).apply(request)
  }

  def getCIsByGroup(id: String, groupId: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvCIsByGroupId(groupId)).mapTo[JsValue]
    } yield processResult(ci, BadRequest, request.session)
    res
  }

  def getBenfordCIsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getBenfordCIsByGroup(id, groupId).apply(request)
  }

  def getBenfordCIsByGroup(id: String, groupId: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvBenfordCIsByGroupId(groupId)).mapTo[JsValue]
    } yield processResult(ci, BadRequest, request.session)
    res
  }

  def getResultsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getResultsByGroup(id, groupId).apply(request)
  }

  def getResultsByGroup(id: String, groupId: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      r <- ask(benfordActor, srvResultsByGroupId(groupId)).mapTo[JsValue]
    } yield processResult(r, BadRequest, request.session)
    res
  }

  def getFreqByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getFreqByGroup(id, groupId).apply(request)
  }

  def getFreqByGroup(id: String, groupId: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvFrequenciesByGroupId(groupId)).mapTo[JsValue]
    } yield processResult(f, BadRequest, request.session)
    res
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

  def getTestsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getTestsByGroup(id, groupId).apply(request)
  }

  def getTestsByGroup(id: String, groupId: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvTestsByGroupId(groupId)).mapTo[JsValue]
    } yield processResult(f, BadRequest, request.session)
    res
  }

  /*
  #
  # BYLEVEL FUNCTIONS ARE NOT YET AVAILABLE IN THE ACTOR
  #
  */
  def getCIsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getCIsByLevel(id, level).apply(request)
  }

  def getCIsByLevel(id: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvCIsByLevel(level)).mapTo[JsValue]
    } yield processResult(ci, BadRequest, request.session)
    res
  }

  def getBenfordCIsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getBenfordCIsByLevel(id, level).apply(request)
  }

  def getBenfordCIsByLevel(id: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvBenfordCIsByLevel(level)).mapTo[JsValue]
    } yield processResult(ci, BadRequest, request.session)
    res
  }

  def getResultsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getResultsByLevel(id, level).apply(request)
  }

  def getResultsByLevel(id: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      r <- ask(benfordActor, srvResultsByLevel(level)).mapTo[JsValue]
    } yield processResult(r, BadRequest, request.session)
    res
  }

  def getFreqByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getFreqByLevel(id, level).apply(request)
  }

  def getFreqByLevel(id: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvFrequenciesByLevel(level)).mapTo[JsValue]
    } yield processResult(f, BadRequest, request.session)
    res
  }

  def getTestsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getTestsByLevel(id, level).apply(request)
  }

  def getTestsByLevel(id: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvTestsByLevel(level)).mapTo[JsValue]
    } yield processResult(f, BadRequest, request.session)
    res
  }

}
