package blended.jms.utils

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import blended.jms.utils.internal._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpecLike, Matchers}

import scala.concurrent.duration._
import scala.util.control.NonFatal

class ConnectionPingActorSpec extends TestKit(ActorSystem("ConnectionPingSpec"))
  with FreeSpecLike
  with Matchers
  with MockitoSugar
  with ImplicitSender {

  "The ConnectionPingActor" - {

    "should respond with a ConnectionHealthy message if the connection is fine" in {

      val testActor = system.actorOf(ConnectionPingActor.props(1.second))

      val healthyPerformer = new PingPerformer(pingActor = testActor, id = "amq") {
        override def ping() = testActor ! PingReceived("Hooray")
      }

      testActor ! healthyPerformer

      expectMsg(PingResult(Right("Hooray")))
    }

    "should respond with a Timeout message if the connection cannot be pinged" in {

      val testActor = system.actorOf(ConnectionPingActor.props(1.second))

      val dirtyPerformer = new PingPerformer(pingActor = testActor, id = "amq") {
        override def ping() = {}
      }

      testActor ! dirtyPerformer

      expectMsg(PingTimeout)
    }

    "should respond with a negative ping result message if the performer throws an exception" in {
      val e = new Exception("boom")

      val testActor = system.actorOf(ConnectionPingActor.props(1.second))

      val dirtyPerformer = new PingPerformer(pingActor = testActor, id = "amq") {
        override def ping() = throw e
      }

      testActor ! dirtyPerformer

      expectMsg(PingResult(Left(e)))
    }
  }
}