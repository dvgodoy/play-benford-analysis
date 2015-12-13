package controllers

import java.util.concurrent.TimeUnit._

import actors.ActorBuffer.Finished
import akka.pattern.ask
import akka.util.Timeout
import java.io.{ByteArrayOutputStream, File}
import javax.imageio.ImageIO
import models.{ImageCommons, SparkCommons}
import models.ImageService._
import play.api.libs.json.{JsUndefined, JsDefined, JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future
import java.net.URL

class ImageSBA extends Controller {
  implicit val timeout = Timeout(30, SECONDS)

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
    val imageActor = ImageCommons.getJob(id + (if (!buffer) "_worker" else ""))
    val res: Future[Result] = if (!buffer) {
      for {
        img <- ask(imageActor, message).mapTo[Finished]
      } yield processResult(img, error, session + ("jobImg", id))
    } else {
      for {
        img <- ask(imageActor, message).mapTo[JsValue]
      } yield processResult(img, error, session + ("jobImg", id))
    }
    res
  }

  def imgUpload = Action.async(parse.multipartFormData) { request =>
    import scala.concurrent.ExecutionContext.Implicits.global

    val uuid = java.util.UUID.randomUUID().toString
    val res = try {
      val imgData = request.body.files(0)
      val filePath = SparkCommons.tmpFolder + "/" + uuid
      imgData.ref.moveTo(new File(filePath))
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", uuid)
      val filePathImg = if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath
      Json.obj("uuid" -> Json.toJson(filePathImg))
    } catch {
      case ex: Exception => Json.obj("error" -> "Error: There was a problem uploading your file. Please try again.")
    }
    Future(processResult(res, NotFound, request.session))
  }

  def loadImageDirect = Action.async(parse.multipartFormData) { request =>
    val id = ImageCommons.createJob

    val imgData = request.body.files(0)
    val result = ImageIO.read(imgData.ref.file)
    val baos = new ByteArrayOutputStream()
    ImageIO.write(result, "png", baos)
    askActor(id, false, request.session, srvDirect(baos), NotAcceptable)
  }

  def loadImageURL = Action.async { request =>
    val id = ImageCommons.createJob

    val url = {
      try {
        request.body.asMultipartFormData.get.dataParts.get("imgURL").get.head
      } catch {
        case ex: Exception => request.body.asFormUrlEncoded.get("imgURL").head
      }
    }

    val filePath = SparkCommons.tmpFolder + "/" + id + ".img"
    val baos = new ByteArrayOutputStream()
    ImageIO.write(ImageIO.read(new URL(url)), "png", baos)
    askActor(id, false, request.session, srvDirect(baos), NotAcceptable)
  }

  def loadImageLocal(filePath: String) = Action.async { request =>
    val id = ImageCommons.createJob
    loadImage(id, filePath).apply(request)
  }

  def loadImage(id: String, filePath: String) = Action.async { request =>
    askActor(id, false, request.session, srvData(filePath), NotAcceptable)
  }

  def calculateSession(windowSize: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    calculate(id, windowSize, async).apply(request)
  }

  def calculate(id: String, windowSize: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvCalc(windowSize), BadRequest)
  }

  def getImageSession(async: Boolean) = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    getImage(id, async).apply(request)
  }

  def getImage(id: String, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvImage(), BadRequest)
  }

  def getSBAImageSession(threshold: Double, whiteBackground: Int, async: Boolean) = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    getSBAImage(id, threshold, whiteBackground, async).apply(request)
  }

  def getSBAImage(id: String, threshold: Double, whiteBackground: Int, async: Boolean) = Action.async { request =>
    askActor(id, async, request.session, srvSBAImage(threshold, whiteBackground == 1), BadRequest)
  }

  /*def loadImageSession = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    val filePath = request.session.get("filePathImg").getOrElse("")
    loadImage(id, filePath).apply(request)
  }*/

  /*def loadImageUploaded(id: String) = Action.async { request =>
    val filePath = if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + SparkCommons.tmpFolder + "/" + id + ".png" else SparkCommons.tmpFolder + "/" + id + ".png"
    loadImage(id, filePath).apply(request)
  }*/

}
