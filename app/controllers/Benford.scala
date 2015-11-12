package controllers

import com.dvgodoy.spark.benford.distributions.{Benford, Bootstrap}
import models.SparkCommons
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class Benf extends Controller {
  lazy val rdd = SparkCommons.sc.parallelize(1 to 1000)

  def PageIndex = Action {
    Ok("hello world")
  }

  /**
   * number of elements
   * @return
   */
  def count = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val boot = Bootstrap()
    val benf = Benford()
    val numSamples = 1000

    val res: Future[Result] = for {
      data <- Future(boot.loadData(SparkCommons.sc, "/media/dvgodoy/FILES/DataScienceRetreat/Portfolio/spark-benford-analysis/src/test/resources/datalevels.csv"))
      sampleRDD <- Future(boot.calcSampleCIs(SparkCommons.sc, data, numSamples))
      benfordRDD <- Future(benf.calcBenfordCIs(SparkCommons.sc, data, numSamples))
      resultsRDD <- Future(boot.calcResults(sampleRDD, benfordRDD))

     ci <- Future(boot.showCIsByGroupId(sampleRDD, 0))
    } yield Ok(ci)
    res
  }

  /**
   * list them all
   * @return
   */
  def list = Action {
    Ok(rdd.collect().toList.toString)
    //Ok(compact(org.json4s.jackson.JsonMethods.render(BenfordFrequencies.toJson("test"))))
    //Ok(BenfordFrequencies.toJson("test")._2)
  }

  /**
   * make a filter action on the rdd and returns the sum of the remaining numbers
   * @param n
   * @return
   */
  def sum(n:String) = Action {
    //Ok(rdd.filter(_ <= n.toInt).sum().toString)
    Ok(JsObject(Seq("test" -> JsNumber(40))))
  }

  def carscounter = Action {
    Ok(JsObject(Seq("test" -> JsNumber(40))))
  }
}
