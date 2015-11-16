package controllers

import javax.inject.Inject

import models.SparkCommons
import play.api.Play.current
import play.api.libs.ws.{WS, WSClient}
import play.api.mvc._
import views.html._

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action { request =>
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    implicit val session = request.session
    Ok(index(uuid)).withSession(("id", uuid), ("job", juuid))
  }

  def progress = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    WS.url(SparkCommons.metricsURL)
      .withHeaders("Accept" -> "application/json")
      .get()
      .map { response => Ok(response.body)}
  }

  /*val html = scala.io.Source.fromURL("https://spark.apache.org/").mkString
  val list = html.split("\n").filter(_ != "")
  val rdds = sc.parallelize(list)
  val count = rdds.filter(_.contains("Spark")).count()*/
}