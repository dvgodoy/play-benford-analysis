package controllers

import javax.inject.Inject

import play.api.libs.ws.WSClient
import play.api.mvc._
import views.html._

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action {
    Ok(index("This is loading..."))
  }

  def ci = Action {
    Ok(cigroup(0))
  }

  /*val html = scala.io.Source.fromURL("https://spark.apache.org/").mkString
  val list = html.split("\n").filter(_ != "")
  val rdds = sc.parallelize(list)
  val count = rdds.filter(_.contains("Spark")).count()*/
}