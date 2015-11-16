package controllers

import com.dvgodoy.spark.benford.util.JobId
import models.BenfordCommons
import play.api.mvc._

import scala.concurrent.Future

class BenfordBootstrap extends Controller {

  def load = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    // generate a new job number when loading new data
    val juuid = java.util.UUID.randomUUID().toString
    implicit val jobId = JobId(juuid)
    val filePath = "/media/dvgodoy/FILES/DataScienceRetreat/Portfolio/spark-benford-analysis/src/test/resources/datalevels.csv"
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.loadData(filePath, jobId))
    } yield Ok(juuid).withSession(("id", uuid), ("job", juuid))
    res
  }

  def calculate(juuid: String, numSamples: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.calculate(numSamples))
    } yield Ok.withSession(("id", uuid), ("job", juuid))
    res
  }

  def getCIsByGroup(juuid: String, id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByGroupId(id, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getBenfordCIsByGroup(juuid: String, id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByGroupId(id, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getCIsByLevel(juuid: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByLevel(level, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getBenfordCIsByLevel(juuid: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByLevel(level, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getResultsByGroup(juuid: String, id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByGroupId(id, jobId))
    } yield Ok(r).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getResultsByLevel(juuid: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByLevel(level, jobId))
    } yield Ok(r).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getFreqByGroup(juuid: String, id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      f <- Future(BenfordCommons.getFrequenciesByGroupId(id, jobId))
    } yield Ok(f).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getFreqByLevel(juuid: String, level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      f <- Future(BenfordCommons.getFrequenciesByLevel(level, jobId))
    } yield Ok(f).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getGroups(juuid: String) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      f <- Future(BenfordCommons.getGroups(jobId))
    } yield Ok(f).withSession(("id", uuid), ("job", juuid))
    res
  }

}
