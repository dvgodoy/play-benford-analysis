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
    val job = request.session.get("job").getOrElse("0")
    implicit val session = request.session
    Ok(index(uuid)).withSession(("id", uuid), ("job", job))
  }

  def progressSession = Action.async { request =>
    val id = request.session.get("job").getOrElse("")
    progress(id).apply(request)
  }

  def progress(id: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    WS.url(SparkCommons.metricsURL)
      .withHeaders("Accept" -> "application/json")
      .get()
      .map { response => Ok(response.body) }
  }

  /*val html = scala.io.Source.fromURL("https://spark.apache.org/").mkString
  val list = html.split("\n").filter(_ != "")*/

  /* How to return an image = Writable[Array[Byte]]
  Ok.stream(Enumerator.fromStream(getClass.getClassLoader.getResourceAsStream("ima‌​ge.gif")))
   */

  /* Serving file from the server to the browser
  def index = Action {
    Ok.sendFile(
      content = new java.io.File("/tmp/fileToServe.pdf"),
      fileName = _ => "termsOfService.pdf"
    )
  }
  def index = Action {
    Ok.sendFile(
      content = new java.io.File("/tmp/fileToServe.pdf"),
      inline = true
    )
  }*/

}