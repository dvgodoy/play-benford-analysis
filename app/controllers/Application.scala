package controllers

import javax.inject.Inject

import play.api.libs.ws.{WSResponse, WSClient}
import play.api.mvc._
import views.html.index
import play.api.libs.json._

import scala.concurrent.Future

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action {
    //import models.{LocalModelsCounterMoto, LocalModelsCounterCars}
    //val counter = LocalModelsCounterCars.counter + LocalModelsCounterMoto.counter
    val counter = 40
    Ok(index(counter))
  }

  case class CounterResponse(counter: Int)
  implicit val crf = Json.format[CounterResponse]
  def counterFromJsonResponse(response: WSResponse): Int = response.json.validate[CounterResponse].get.counter

  def rootremote = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val carsCounterResponse: Future[WSResponse] = ws.url("http://localhost:9000/carscounter").get
    val motoCounterResponse: Future[WSResponse] = ws.url("http://localhost:9000/motocounter").get

    val res: Future[Result] = for {
      cars <- carsCounterResponse.map(counterFromJsonResponse)
      motos <- motoCounterResponse.map(counterFromJsonResponse)
    } yield Ok(index(cars + motos))
    res
  }
}