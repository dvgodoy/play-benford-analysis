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

  def libs: Seq[String] = {
    val libDir = play.api.Play.application.getFile("lib")

    return if ( libDir.exists ) {
      libDir.listFiles().map(_.getCanonicalFile().getAbsolutePath()).filter(_.endsWith(".jar"))
    } else {
      throw new IllegalStateException(s"lib dir is missing: $libDir")
    }
  }

  //build the SparkConf  object at once
  lazy val conf = {
    new SparkConf(false)
      //.setMaster("spark://<MASTER_IP>:<MASTER_PORT>")
      //.setMaster("spark://MONSTER:7077")
      .setMaster("local[*]")
      .setAppName("play demo")
      .set("spark.logConf", "true")
      .setJars(libs)
  }

  lazy val sc = SparkContext.getOrCreate(conf)
  lazy val sqlContext = new SQLContext(sc)

}
