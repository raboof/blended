package blended.file

import java.io.File

import akka.actor.Props
import akka.testkit.TestProbe
import blended.testsupport.TestActorSys
import org.scalatest.{FreeSpec, Matchers}

class FileProcessActorSpec extends FreeSpec with Matchers {

  "The FileProcessActor should" - {

    "process a single file and delete it on success if no archive dir is set" in TestActorSys { testkit =>

      implicit val system = testkit.system

      val cfg = FilePollConfig(system.settings.config.getConfig("blended.file.poll")).copy(
        sourceDir = System.getProperty("projectTestOutput") + "/actor"
      )

      val srcFile = new File(cfg.sourceDir, "test.txt")

      val probe = TestProbe()
      val evtProbe = TestProbe()

      system.eventStream.subscribe(evtProbe.ref, classOf[FileProcessed])

      val cmd = FileProcessCmd(new File(cfg.sourceDir, "test.txt"), cfg, new SucceedingFileHandler())

      system.actorOf(Props[FileProcessActor]).tell(cmd, probe.ref)

      probe.expectMsg(FileProcessed(cmd, true))
      evtProbe.expectMsg(FileProcessed(cmd, true))

      srcFile.exists() should be (false)
    }

    "process a single file and move it to the archive dir if an archive dir is set" in TestActorSys { testkit =>
      implicit val system = testkit.system

      val archiveDir = new File(System.getProperty("projectTestOutput") + "/archive")
      archiveDir.mkdirs()

      val cfg = FilePollConfig(system.settings.config.getConfig("blended.file.poll")).copy(
        sourceDir = System.getProperty("projectTestOutput") + "/actor",
        backup = Some(archiveDir.getAbsolutePath())
      )

      val srcFile = new File(cfg.sourceDir, "test.xml")

      val probe = TestProbe()
      val evtProbe = TestProbe()

      system.eventStream.subscribe(evtProbe.ref, classOf[FileProcessed])

      val cmd = FileProcessCmd(srcFile, cfg, new SucceedingFileHandler())

      system.actorOf(Props[FileProcessActor]).tell(cmd, probe.ref)

      probe.expectMsg(FileProcessed(cmd, true))
      evtProbe.expectMsg(FileProcessed(cmd, true))

      new File(archiveDir, "test.xml").exists() should be (true)
      srcFile.exists() should be (false)
    }

    "Restore the original file if the FilePollHandler throws an Exception" in TestActorSys { testkit =>

      implicit val system = testkit.system

      val cfg = FilePollConfig(system.settings.config.getConfig("blended.file.poll")).copy(
        sourceDir = System.getProperty("projectTestOutput") + "/poll",
        tmpExt = "_temp"
      )

      val srcFile = new File(cfg.sourceDir, "test.txt")

      val probe = TestProbe()
      val evtProbe = TestProbe()

      system.eventStream.subscribe(evtProbe.ref, classOf[FileProcessed])

      val cmd = FileProcessCmd(srcFile, cfg, new FailingFileHandler())

      system.actorOf(Props[FileProcessActor]).tell(cmd, probe.ref)

      probe.expectMsg(FileProcessed(cmd, false))
      evtProbe.expectMsg(FileProcessed(cmd, false))

      srcFile.exists() should be (true)
    }
  }

}
