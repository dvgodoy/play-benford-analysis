package controllers

import javax.inject.Inject

import models.SparkCommons
import play.api.Play.current
import play.api.libs.ws.{WS, WSClient}
import play.api.mvc._
import views.html._

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action {
    Ok(index("This is loading..."))
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