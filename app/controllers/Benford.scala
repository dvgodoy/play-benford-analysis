package controllers

import com.dvgodoy.spark.benford.constants._
import models.SparkCommons
import play.api.libs.json.{JsNumber, JsObject}
import play.api.mvc.{Action, Controller}

class Benford extends Controller {
  lazy val rdd = SparkCommons.sc.parallelize(1 to 1000)

  def index = Action {
    Ok("hello world")
  }

  /**
   * number of elements
   * @return
   */
  def count = Action {
    Ok(rdd.count().toString)
  }

  /**
   * list them all
   * @return
   */
  def list = Action {
    //Ok(rdd.collect().toList.toString)
    Ok(BenfordProbabilitiesD1D2.toList.toString)
  }

  /**
   * make a filter action on the rdd and returns the sum of the remaining numbers
   * @param n
   * @return
   */
  def sum(n:String) = Action {
    Ok(rdd.filter(_ <= n.toInt).sum().toString)
  }

  def carscounter = Action {
    Ok(JsObject(Seq("counter" -> JsNumber(40))))
  }
}
