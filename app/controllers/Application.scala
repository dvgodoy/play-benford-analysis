package controllers

import javax.inject.Inject

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import views.html._

import scala.concurrent.Future

class Application @Inject()(ws: WSClient) extends Controller {

  def root = Action {
    //import models.{LocalModelsCounterMoto, LocalModelsCounterCars}
    //val counter = LocalModelsCounterCars.counter + LocalModelsCounterMoto.counter
    val counter = 40
    Ok(index("test"))
  }

  case class CounterResponse(test: Int)
  implicit val crf = Json.reads[CounterResponse]
  def counterFromJsonResponse(response: WSResponse): Int = response.json.validate[CounterResponse].get.test

  case class SubResp(first: Int, second: Int)
  case class Resp(another: SubResp, more: Int)

  implicit val SubRespWrites = new Writes[SubResp] {
    def writes(sub: SubResp) = Json.obj(
      "first" -> sub.first,
      "second" -> sub.second
    )
  }
  implicit val RespWrites = new Writes[Resp] {
    def writes(resp: Resp) = Json.obj(
      "another" -> resp.another,
      "more" -> resp.more
    )
  }
  val test = Resp(
    SubResp(17,20),
    9
  )
  val json = Json.toJson(test)

  implicit val SubRespReads: Reads[SubResp] = (
    (JsPath \ "first").read[Int] and
    (JsPath \ "second").read[Int]
  )(SubResp.apply _)

  implicit val RespReads: Reads[Resp] = (
    (JsPath \ "another").read[SubResp] and
    (JsPath \ "more").read[Int]
  )(Resp.apply _)

  //implicit val crf2 = Json.reads[Resp]
  def counterFromJson(response: JsValue): Resp = response.validate[Resp].get

  def rootJson = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val motoCounterResponse: Future[JsValue] = Future(json)

    val res: Future[Result] = for {
      motos <- motoCounterResponse.map(identity)
    } yield Ok(motos)
    res
  }

  def remoteJson = Action {
    Ok(index(""))
  }

  def rootremote = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val carsCounterResponse: Future[WSResponse] = ws.url("http://localhost:9000/carscounter").get
    //val motoCounterResponse: Future[JsObject] = Future(JsObject(Seq("another" -> JsNumber(39))))

    //val motoCounterResponse: Future[WSResponse] = ws.url("http://localhost:9000/motocounter").get

    val res: Future[Result] = for {
      cars <- carsCounterResponse.map(counterFromJsonResponse)
      //motos <- motoCounterResponse.map(counterFromJsonResponse)
      //motos <- motoCounterResponse.map(counterFromJson)
    } yield Ok(index(""))
    res
  }
}