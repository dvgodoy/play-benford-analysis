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
    val jobId = JobId(juuid)
    val filePath = "/media/dvgodoy/FILES/DataScienceRetreat/Portfolio/spark-benford-analysis/src/test/resources/datalevels.csv"
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.loadData(filePath, jobId))
    } yield Ok("").withSession(("id", uuid), ("job", juuid))
    res
  }

  def calculate = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    BenfordCommons.setNumberSamples(1000)
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.calculate)
    } yield Ok(request.session.get("job").getOrElse("0"))
    res
  }

  def getCIsByGroup(id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByGroupId(id, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getBenfordCIsByGroup(id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByGroupId(id, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getCIsByLevel(level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByLevel(level, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getBenfordCIsByLevel(level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByLevel(level, jobId))
    } yield Ok(ci).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getResultsByGroup(id: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByGroupId(id, jobId))
    } yield Ok(r).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getResultsByLevel(level: Int) = Action.async { request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val uuid = request.session.get("id").getOrElse(java.util.UUID.randomUUID().toString)
    val juuid = request.session.get("job").getOrElse("0")
    val jobId = JobId(juuid)
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByLevel(level, jobId))
    } yield Ok(r).withSession(("id", uuid), ("job", juuid))
    res
  }

  def getFreqByGroup(id: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      f <- Future(BenfordCommons.getFrequenciesByGroupId(id))
    } yield Ok(f)
    res
  }

  def getFreqByLevel(level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      f <- Future(BenfordCommons.getFrequenciesByLevel(level))
    } yield Ok(f)
    res
  }

}
