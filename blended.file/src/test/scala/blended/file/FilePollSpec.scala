package blended.file

import akka.testkit.{ImplicitSender, TestProbe}
import blended.testsupport.TestActorSys
import org.scalatest.{DoNotDiscover, FreeSpec, Matchers}

import scala.concurrent.duration._

@DoNotDiscover
class FilePollSpec extends FreeSpec with Matchers {

  "The File Poller should" - {

    "do something" in TestActorSys { testkit =>

      implicit val system = testkit.system

      val cfg = system.settings.config.getConfig("blended.file.poll")

      val actor = system.actorOf(FilePollActor.props(FilePollConfig(cfg), new TestFileHandler()))

      val probe = TestProbe()

      probe.fishForMessage ( max = 10.seconds ) {
        case _ => true
      }

    }
  }


}
