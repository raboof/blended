package de.woq.blended.jolokia

import akka.actor.Props
import akka.testkit.TestActorRef
import akka.util.Timeout
import de.woq.blended.jolokia.model._
import de.woq.blended.testsupport.TestActorSys
import org.scalatest.{Matchers, WordSpecLike}
import scala.concurrent.duration._

import de.woq.blended.jolokia.protocol._

import scala.util.Failure

class JolokiaJVM extends JolokiaClient with JolokiaAddress {
  override val jolokiaUrl = "http://localhost:7777/jolokia"
}

class JolokiaFake extends JolokiaClient with JolokiaAddress {
  override val jolokiaUrl = "http://localhost:8888/jolokia"
}

class JolokiaClientSpec extends TestActorSys
  with WordSpecLike
  with Matchers {

  implicit val timeout = new Timeout(3.seconds)

  "The Jolokia client" should {

    "Connect to Jolokia" in {
      val jolokia = TestActorRef(Props[JolokiaJVM])
      jolokia ! GetJolokiaVersion
      expectMsgAnyClassOf(classOf[JolokiaVersion])
    }

    "Allow to search for MBeans" in {
      val jolokia = TestActorRef(Props[JolokiaJVM])
      jolokia ! SearchJolokia("java.lang:*")
      fishForMessage() {
        case JolokiaSearchResult(mbeanNames) => mbeanNames.size > 0
        case _ => false
      }
    }

    "Allow to read a specific MBean" in {
      val jolokia = TestActorRef(Props[JolokiaJVM])
      jolokia ! ReadJolokiaMBean("java.lang:type=Memory")
      fishForMessage() {
        case JolokiaReadResult(objName, _) => objName == "java.lang:type=Memory"
        case _ => false
      }
    }
    
    "Respond with a failure if the rest call fails" in {
      val jolokia = TestActorRef(Props[JolokiaFake])
      jolokia ! GetJolokiaVersion
      fishForMessage() {
        case Failure(error) => true
      }
    }
  }

}