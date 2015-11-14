package controllers

import models.BenfordCommons
import play.api.mvc._

import scala.concurrent.Future

class BenfordBootstrap extends Controller {

  def load = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val filePath = "/media/dvgodoy/FILES/DataScienceRetreat/Portfolio/spark-benford-analysis/src/test/resources/datalevels.csv"
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.loadData(filePath))
    } yield Ok("load OK")
    res
  }

  def calculate = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    BenfordCommons.setNumberSamples(1000)
    val res: Future[Result] = for {
      ok <- Future(BenfordCommons.calculate)
    } yield Ok("calc OK")
    res
  }

  def getCIsByGroup(id: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByGroupId(id))
    } yield Ok(ci)
    res
  }

  def getBenfordCIsByGroup(id: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByGroupId(id))
    } yield Ok(ci)
    res
  }

  def getCIsByLevel(level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getCIsByLevel(level))
    } yield Ok(ci)
    res
  }

  def getBenfordCIsByLevel(level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      ci <- Future(BenfordCommons.getBenfordCIsByLevel(level))
    } yield Ok(ci)
    res
  }

  def getResultsByGroup(id: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByGroupId(id))
    } yield Ok(r)
    res
  }

  def getResultsByLevel(level: Int) = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    val res: Future[Result] = for {
      r <- Future(BenfordCommons.getResultsByLevel(level))
    } yield Ok(r)
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
