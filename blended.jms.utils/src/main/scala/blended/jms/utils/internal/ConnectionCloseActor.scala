package blended.jms.utils.internal

import akka.actor._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object ConnectionCloseActor {
  def props(holder: ConnectionHolder, retryInterval : FiniteDuration = 5.seconds) =
    new ConnectionCloseActor(holder, retryInterval)
}

/**
  * This Actor will execute and monitor a single close operation on a given JMS connection
  * and then stop itself.
  * @param holder
  */
class ConnectionCloseActor(holder: ConnectionHolder, retryInterval: FiniteDuration) extends Actor with ActorLogging {

  private[this] implicit val eCtxt : ExecutionContext = context.system.dispatcher

  case object Tick

  private[this] def doClose() : Unit = {
    Future {
      holder.close() match {
        case Success(_) => self ! ConnectionClosed
        case Failure(t) =>
          log.warning(s"Error closing connection [${holder.vendor}:${holder.provider}] : [${t.getMessage()}]")
          context.system.scheduler.scheduleOnce(retryInterval, self, Tick)
      }
    }
  }

  override def receive: Receive = {

    case Disconnect(t) =>
      // Just schedule a timeout message in case the Future takes too long
      context.become(waiting(sender(), context.system.scheduler.scheduleOnce(t, self, CloseTimeout)))
      doClose()
  }

  def waiting(caller: ActorRef, timer: Cancellable) : Receive = {

    case Tick =>
      doClose()

    case CloseTimeout =>
      timer.cancel()
      caller ! CloseTimeout
      context.stop(self)

    case ConnectionClosed =>
      timer.cancel()
      caller ! ConnectionClosed
      context.stop(self)
  }
}
