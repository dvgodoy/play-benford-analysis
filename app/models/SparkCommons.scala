package models

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import play.api.Play.current

/**
 * Handles configuration, context and so
 *
 * @author Daniel Voigt Godoy.
 */
object SparkCommons {
  val appName = "PlayBenford"
  /*val hadoop = true
  val localMode = false
  val masterIP = "ip-172-31-0-199"*/
  val hadoop = false
  val localMode = true
  val masterIP = "MONSTER"
  val masterPort = 7077
  val metricsPort = 4040
  val masterURL = if (localMode) "local[*]" else "spark://" + masterIP + ":" + masterPort.toString
  val metricsURL = "http://" + masterIP + ":" + metricsPort.toString + "/api/v1/applications/" + appName + "/jobs"
  val tmpFolder = "/tmp"

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

  def copyToHdfs(localPath: String, fileName: String) = {
    val hdfsConfig = new Configuration
    val hdfsURI = "hdfs://" + SparkCommons.masterIP + ":9000"
    val hdfs = FileSystem.get(new URI(hdfsURI), hdfsConfig)
    val destFile = hdfsURI + SparkCommons.tmpFolder + "/" + fileName
    val targetPath = new Path(destFile)
    if (hdfs.exists(targetPath)) {
      hdfs.delete(targetPath, true)
    }
    val oriPath = new Path(localPath + fileName)
    hdfs.copyFromLocalFile(oriPath, targetPath)
    hdfs.close()
  }
}
