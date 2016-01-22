package unit

import ActorBufferSpec._
import actors.ActorBuffer
import actors.ActorBuffer._
import akka.actor.{ Actor, ActorSystem, Props }
import akka.pattern.pipe
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import scala.concurrent.duration._
import scala.concurrent.Future

class ActorBufferSpec extends TestKit(ActorBufferSpec.system) with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  val testActorRef = system.actorOf(Props(classOf[ActorAuxBuffer]), name = "test")

  override def afterAll {
    shutdown()
  }

  "An ActorAuxBuffer" should {
    "Respond with STARTED_MSG when receiving any message" in {
      within(500 millis) {
        testActorRef ! srvComplete()
        expectMsg(ActorBuffer.STARTED_MSG)
      }
    }

    "Respond with RESULT when receiving the same message for a finished operation" in {
      within(500 millis) {
        testActorRef ! srvComplete()
        expectMsg("")
      }
    }

    "Respond with PROCESSING_MSG when receiving the same message for an unfinished operation" in {
      within(500 millis) {
        testActorRef ! srvStart()
        expectMsg(ActorBuffer.STARTED_MSG)
        testActorRef ! srvStart()
        expectMsg(ActorBuffer.PROCESSING_MSG)
      }
    }
  }
}

object ActorBufferSpec {
  val system = ActorSystem("ActorBufferSpec")

  case class srvStart()
  case class srvComplete()
  case class generalBuffer()

  class ActorAux() extends Actor {
    import context.dispatcher

    def receive = {
      case srvStart() => {
        Future("") pipeTo sender
      }
      case srvComplete() => {
        Future("") map (Finished(srvComplete(), _)) pipeTo sender
      }
    }
  }

  class ActorAuxBuffer extends Actor with ActorBuffer {
    override def preStart(): Unit = {
      setWorker(system, classOf[ActorAux])
      super.preStart()
    }
  }

}