package akka

import akka.actor.{PoisonPill, Cancellable, Actor}

trait ActorTimer extends Actor {

  import context.dispatcher

  private var scheduler: Cancellable = _

  def sendPoisonPill(): Unit = {
    import scala.concurrent.duration._
    scheduler = context.system.scheduler.schedule(
      initialDelay = 30 minutes,
      interval = 0 seconds,
      receiver = self,
      message = PoisonPill
    )
  }

  override def preStart():Unit = {
    sendPoisonPill()
  }

  override def postStop(): Unit = {
    scheduler.cancel()
  }

  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = msg match {
    case msg => {
      // if there is a new message, cancel any previous poison pill
      scheduler.cancel()
      sendPoisonPill()
      super.aroundReceive(receive, msg)
    }
  }
}