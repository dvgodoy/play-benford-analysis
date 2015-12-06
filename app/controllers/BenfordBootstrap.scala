package controllers

import java.util.concurrent.TimeUnit._

import akka.pattern.ask
import akka.util.Timeout
import java.io.File
import models.{SparkCommons, BenfordCommons}
import models.BenfordService._
import org.scalactic._
import play.api.libs.json.{JsUndefined, JsDefined, JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future

class BenfordBootstrap extends Controller {

  def accUpload = Action(parse.multipartFormData) { request =>
    val id = BenfordCommons.createJob
    request.body.file("accData").map { accData =>
      val filePath = SparkCommons.tmpFolder + "/" + id + ".csv"
      accData.ref.moveTo(new File(filePath))
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".csv")
      Ok("").withSession(request.session + ("job", id) + ("filePath", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath))
      //Ok("").withSession(("job", id), ("filePath", filePath))
    }.getOrElse {
      //Redirect(routes.Application.root).flashing(
      //  "error" -> "Missing file"
      //)
      NotFound("")
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
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      rsp <- ask(benfordActor, srvData(filePath)).mapTo[Or[JsValue, Every[ErrorMessage]]]
    } yield rsp match {
        case Good(s) => Ok(Json.toJson(s)).withSession(request.session + ("job", id))
        case Bad(e) =>  NotAcceptable(Json.obj("error" -> Json.toJson(e.head))).withSession(request.session + ("jobImg", id))
      }
    res
  }

  def getGroupsSession = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getGroups(id).apply(request)
  }

  def getGroups(id: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvGroups()).mapTo[JsValue]
    //} yield Ok(f)
    } yield (f \ "error") match {
        case msg: JsDefined => BadRequest(msg.get)
        case res: JsUndefined => Ok(f)
      }
    res
  }

  def calculateSession(numSamples: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    calculate(id, numSamples).apply(request)
  }

  def calculate(id: String, numSamples: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      rsp <- ask(benfordActor, srvCalc(numSamples)).mapTo[Or[JsValue, Every[ErrorMessage]]]
    } yield rsp match {
        case Good(s) => Ok(Json.toJson(s))
        case Bad(e) => BadRequest(Json.obj("error" -> Json.toJson(e.head)))
      }
    res
  }

  def getCIsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getCIsByGroup(id, groupId).apply(request)
  }

  def getCIsByGroup(id: String, groupId: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvCIsByGroupId(groupId)).mapTo[JsValue]
    } yield Ok(ci)
    res
  }

  def getBenfordCIsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getBenfordCIsByGroup(id, groupId).apply(request)
  }

  def getBenfordCIsByGroup(id: String, groupId: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvBenfordCIsByGroupId(groupId)).mapTo[JsValue]
    } yield Ok(ci)
    res
  }

  def getCIsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getCIsByLevel(id, level).apply(request)
  }

  def getCIsByLevel(id: String, level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvCIsByLevel(level)).mapTo[JsValue]
    } yield Ok(ci)
    res
  }

  def getBenfordCIsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getBenfordCIsByLevel(id, level).apply(request)
  }

  def getBenfordCIsByLevel(id: String, level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      ci <- ask(benfordActor, srvBenfordCIsByLevel(level)).mapTo[JsValue]
    } yield Ok(ci)
    res
  }

  def getResultsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getResultsByGroup(id, groupId).apply(request)
  }

  def getResultsByGroup(id: String, groupId: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      r <- ask(benfordActor, srvResultsByGroupId(groupId)).mapTo[JsValue]
    } yield Ok(r)
    res
  }

  def getResultsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getResultsByLevel(id, level).apply(request)
  }

  def getResultsByLevel(id: String, level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      r <- ask(benfordActor, srvResultsByLevel(level)).mapTo[JsValue]
    } yield Ok(r)
    res
  }

  def getFreqByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getFreqByGroup(id, groupId).apply(request)
  }

  def getFreqByGroup(id: String, groupId: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvFrequenciesByGroupId(groupId)).mapTo[JsValue]
    } yield Ok(f)
    res
  }

  def getFreqByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getFreqByLevel(id, level).apply(request)
  }

  def getFreqByLevel(id: String, level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvFrequenciesByLevel(level)).mapTo[JsValue]
    } yield Ok(f)
    res
  }

  def getExactBenfordParams = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      p <- Future(BenfordCommons.getExactBenfordParams)
    } yield Ok(p)
    res
  }

  def getExactBenfordProbs = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      p <- Future(BenfordCommons.getExactBenfordProbs)
    } yield Ok(p)
    res
  }

  def getTestsByGroupSession(groupId: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getTestsByGroup(id, groupId).apply(request)
  }

  def getTestsByGroup(id: String, groupId: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvTestsByGroupId(groupId)).mapTo[JsValue]
    } yield Ok(f)
    res
  }

  def getTestsByLevelSession(level: Int) = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    getTestsByLevel(id, level).apply(request)
  }

  def getTestsByLevel(id: String, level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val benfordActor = BenfordCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      f <- ask(benfordActor, srvTestsByLevel(level)).mapTo[JsValue]
    } yield Ok(f)
    res
  }

}
