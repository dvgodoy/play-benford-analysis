package models

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import play.api.Play.current

/**
 * Handles configuration, context and so
 *
 * @author Daniel Voigt Godoy.
 */
object SparkCommons {
  val r = scala.util.Random
  val appName = "PlayBenford"
  val localMode = true
  val masterIP = "MONSTER"
  val masterPort = 7077
  val metricsPort = 4040
  val masterURL = if (localMode) "local[*]" else "spark://" + masterIP + masterPort.toString
  val metricsURL = "http://" + masterIP + ":" + metricsPort.toString + "/api/v1/applications/" + appName + "/jobs"

  def libs: Seq[String] = {
    val libDir = play.api.Play.application.getFile("lib")

    return if ( libDir.exists ) {
      libDir.listFiles().map(_.getCanonicalFile().getAbsolutePath()).filter(_.endsWith(".jar"))
    } else {
      throw new IllegalStateException(s"lib dir is missing: $libDir")
    }
  }

  //build the SparkConf object at once
  lazy val conf = {
    new SparkConf(false)
      .setMaster(masterURL)
      .setAppName(appName)
      .set("spark.logConf", "true")
      .setJars(libs)
  }

  lazy val sc = SparkContext.getOrCreate(conf)
  lazy val sqlContext = new SQLContext(sc)

}
