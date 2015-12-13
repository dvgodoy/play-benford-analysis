package controllers

import javax.inject.Inject

import models.SparkCommons
import play.api.Play.current
import play.api.libs.ws.{WS, WSClient}
import play.api.mvc._
import views.html._

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action { request =>
    Ok(index(""))
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

}