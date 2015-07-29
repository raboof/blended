package blended.updater.config

import org.scalatest.FreeSpecLike
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import scala.util.Success
import scala.util.Failure
import blended.testsupport.TestFile
import java.io.File
import scala.io.Source
import java.io.FileWriter

class RuntimeConfigTest
    extends FreeSpecLike
    with TestFile {

  implicit val deletePolicy = TestFile.DeleteWhenNoFailure

  "Minimal config" - {

    val minimal = """
      |name = name
      |version = 1.0.0
      |bundles = [{ url = "http://example.org", jarName = "bundle1.jar", sha1Sum = sum, startLevel = 0 }]
      |startLevel = 10
      |defaultStartLevel = 10
      |""".stripMargin

    "read" in {
      val config = RuntimeConfig.read(ConfigFactory.parseString(minimal)).get
    }

    val lines = minimal.trim().split("\n")
    0.to(lines.size - 1).foreach { n =>
      "without line " + n + " must fail" in {
        val config = lines.take(n) ++ lines.drop(n + 1)
        val ex = intercept[RuntimeException] {
          RuntimeConfig.read(ConfigFactory.parseString(config.mkString("\n"))).get
        }
        assert(ex.isInstanceOf[ConfigException.ValidationFailed] || ex.isInstanceOf[IllegalArgumentException])
      }
    }

    "read -> toConfig -> read must result in same config" in {
      import RuntimeConfig._
      val config = read(ConfigFactory.parseString(minimal))
      assert(config === read(toConfig(config.get)))
    }
  }

  "resolveFileName" - {
    "should infer the correct filename from a file URL" in {
      val bundle = BundleConfig(url = "file:///tmp/file1.jar", start = false, startLevel = 0)
      val rc = RuntimeConfig(name = "test", version = "1", bundles = List(bundle), startLevel = 1, defaultStartLevel = 1)
      assert(rc.resolveFileName(bundle.url) === Success("file1.jar"))
    }
    "should infer the correct filename from a http URL" in {
      val bundle = BundleConfig(url = "http:///tmp/file1.jar", start = false, startLevel = 0)
      val rc = RuntimeConfig(name = "test", version = "1", bundles = List(bundle), startLevel = 1, defaultStartLevel = 1)
      assert(rc.resolveFileName(bundle.url) === Success("file1.jar"))
    }
    "should infer the correct filename from a mvn URL without a repo setting" in {
      val bundle = BundleConfig(url = "mvn:group:file:1", start = false, startLevel = 0)
      val rc = RuntimeConfig(name = "test", version = "1", bundles = List(bundle), startLevel = 1, defaultStartLevel = 1)
      assert(rc.resolveFileName(bundle.url) === Success("file-1.jar"))
    }

  }

  "Property file generation" - {

    val bundle0 = BundleConfig(url = "http://b0.jar", startLevel = 0)
    val prev = RuntimeConfig(name = "test", version = "1", startLevel = 5, defaultStartLevel = 5, bundles = List(bundle0))
    val next = prev.copy(version = "2")

    "should not write a properties file without required settings" in {
      withTestDir() { dir =>
        val res = RuntimeConfig.createPropertyFile(prev, Option(next), dir)
        assert(res === None)
      }
    }

    "should write properties file without a previous config" in {
      withTestDir() { dir =>
        sys.props += "TEST_A" -> "TEST_a"
        sys.props += "test.prop" -> "TEST_PROP"
        val res = RuntimeConfig.createPropertyFile(next.copy(properties = Map(
          RuntimeConfig.Properties.PROFILE_PROPERTY_FILE -> "etc/props",
          RuntimeConfig.Properties.PROFILE_PROPERTY_PROVIDERS -> "sysprop",
          RuntimeConfig.Properties.PROFILE_PROPERTY_KEYS -> "TEST_A,test.prop"
        )), None, dir)
        val expectedTargetFile = new File(dir, "test/2/etc/props")
        assert(res === Some(Success(expectedTargetFile)))
        assert(Source.fromFile(expectedTargetFile).getLines.drop(2).toSet === Set("TEST_A=TEST_a", "test.prop=TEST_PROP"))
        sys.props -= "TEST_A"
        sys.props -= "test.prop"
      }
    }

    "should write properties file from previous config" in {
      withTestDir() { dir =>
        val sourceFile = new File(dir, "test/1/etc/props")
        val expectedTargetFile = new File(dir, "test/2/etc/props") {
          sourceFile.getParentFile().mkdirs()
          val w = new FileWriter(sourceFile)
          w.append("test.prop=TEST_PROP")
          w.close()
        }

        val res = RuntimeConfig.createPropertyFile(next.copy(properties = Map(
          RuntimeConfig.Properties.PROFILE_PROPERTY_FILE -> "etc/props",
          RuntimeConfig.Properties.PROFILE_PROPERTY_PROVIDERS -> "fileCurVer:etc/props",
          RuntimeConfig.Properties.PROFILE_PROPERTY_KEYS -> "test.prop"
        )), Some(prev), dir)
        assert(res === Some(Success(expectedTargetFile)))
        assert(Source.fromFile(expectedTargetFile).getLines.drop(2).toSet === Set("test.prop=TEST_PROP"))
      }
    }
  }

}