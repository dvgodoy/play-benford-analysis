package controllers

import java.util.concurrent.TimeUnit._

import akka.pattern.ask
import akka.util.Timeout
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.imageio.ImageIO
import models.{ImageCommons, SparkCommons}
import models.ImageService._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future

class ImageSBA extends Controller {

  def imgUpload = Action(parse.multipartFormData) { request =>
    val id = ImageCommons.createJob
    request.body.file("imgData").map { imgData =>
      val filePath = SparkCommons.tmpFolder + "/" + id + ".png"
      imgData.ref.moveTo(new File(filePath))
      if (SparkCommons.hadoop) SparkCommons.copyToHdfs(SparkCommons.tmpFolder + "/", id + ".png")
      Ok("").withSession(request.session + ("jobImg", id) + ("filePathImg", if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + filePath else filePath))
    }.getOrElse {
      //Redirect(routes.Application.root).flashing(
      //  "error" -> "Missing file"
      //)
      NotFound("")
    }
  }

  def loadImageDirect = Action.async(parse.multipartFormData) { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val id = ImageCommons.createJob
    val imageActor = ImageCommons.getJob(id)

    val imgData = request.body.files(0)
    val result = ImageIO.read(imgData.ref.file)
    val baos = new ByteArrayOutputStream()
    ImageIO.write(result, "png", baos)
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      img <- ask(imageActor, srvDirect(baos)).mapTo[String]
    } yield Ok(Json.toJson(img)).withSession(request.session + ("jobImg", id))
    res
  }

  def loadImageSession = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    val filePath = request.session.get("filePathImg").getOrElse("")
    loadImage(id, filePath).apply(request)
  }

  def loadImageUploaded(id: String) = Action.async { request =>
    val filePath = if (SparkCommons.hadoop) "hdfs://" + SparkCommons.masterIP + ":9000" + SparkCommons.tmpFolder + "/" + id + ".png" else "file://" + SparkCommons.tmpFolder + "/" + id + ".png"
    loadImage(id, filePath).apply(request)
  }

  def loadImageLocal(filePath: String) = Action.async { request =>
    val id = ImageCommons.createJob
    loadImage(id, filePath).apply(request)
  }

  def loadImage(id: String, filePath: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val imageActor = ImageCommons.getJob(id)
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      res <- ask(imageActor, srvData(filePath)).mapTo[String]
    } yield Ok(Json.toJson(res)).withSession(request.session + ("jobImg", id))
    res
  }

  def calculateSession(windowSize: Int) = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    calculate(id, windowSize).apply(request)
  }

  def calculate(id: String, windowSize: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val imageActor = ImageCommons.getJob(id)
    implicit val timeout = Timeout(1, MINUTES)
    val res: Future[Result] = for {
      res <- ask(imageActor, srvCalc(windowSize)).mapTo[String]
    } yield Ok(Json.toJson(res))
    res
  }

  def getImageSession() = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    getImage(id).apply(request)
  }

  def getImage(id: String) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val imageActor = ImageCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      img <- ask(imageActor, srvImage()).mapTo[String]
    } yield Ok(img)
    res
  }

  def getSBAImageSession(threshold: Double, whiteBackground: Int) = Action.async { request =>
    val id = request.session.get("jobImg").getOrElse("")
    getSBAImage(id, threshold, whiteBackground).apply(request)
  }

  def getSBAImage(id: String, threshold: Double, whiteBackground: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val imageActor = ImageCommons.getJob(id)
    implicit val timeout = Timeout(15, MINUTES)
    val res: Future[Result] = for {
      img <- ask(imageActor, srvSBAImage(threshold, whiteBackground == 1)).mapTo[String]
    } yield Ok(img)
      /*Ok.sendFile(
        content = new java.io.File(img),
        inline = true
      )*/
    res
  }

}
