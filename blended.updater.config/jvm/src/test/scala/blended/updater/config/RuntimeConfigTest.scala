package blended.updater.config

import blended.testsupport.TestFile
import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.{FreeSpecLike, Matchers}

import scala.io.Source
import scala.util.Success

class RuntimeConfigTest
    extends FreeSpecLike
    with Matchers
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
      val config = RuntimeConfigCompanion.read(ConfigFactory.parseString(minimal)).get
    }

    val lines = minimal.trim().split("\n")
    0.to(lines.size - 1).foreach { n =>
      "without line " + n + " must fail" in {
        val config = lines.take(n) ++ lines.drop(n + 1)
        val ex = intercept[RuntimeException] {
          ResolvedRuntimeConfig(RuntimeConfigCompanion.read(ConfigFactory.parseString(config.mkString("\n"))).get, List())
        }
        assert(ex.isInstanceOf[ConfigException] || ex.isInstanceOf[IllegalArgumentException])
      }
    }

    "read -> toConfig -> read must result in same config" in {
      import RuntimeConfigCompanion._
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

  "A Config with features references" - {
    val config = """
      |name = name
      |version = 1
      |bundles = [{url = "mvn:base:bundle1:1"}]
      |startLevel = 10
      |defaultStartLevel = 10
      |features = [
      |  { name = feature1, version = 1 }
      |  { name = feature2, version = 1 }
      |]
      |""".stripMargin

    val feature1 = """
      |name = feature1
      |version = 1
      |bundles = [{url = "mvn:feature1:bundle1:1"}]
      |""".stripMargin

    val feature2 = """
      |name = feature2
      |version = 1
      |bundles = [{url = "mvn:feature2:bundle1:1"}]
      |features = [{name = feature3, version = 1}]
      |""".stripMargin

    val feature3 = """
      |name = feature3
      |version = 1
      |bundles = [{url = "mvn:feature3:bundle1:1", startLevel = 0}]
      |""".stripMargin

    def features = List(feature1, feature2, feature3).map(f => {
      val fc = FeatureConfigCompanion.read(ConfigFactory.parseString(f))
      fc shouldBe a[Success[_]]
      fc.get
    })

    //    "should read but not validate" in {
    //      val rcTry = RuntimeConfig.read(ConfigFactory.parseString(config))
    //      rcTry shouldBe a[Success[_]]
    //
    //      val rc = rcTry.get
    //      val rrc = ResolvedRuntimeConfig(rc, features.take(1))
    //      rc.bundles should have size (1)
    //      rc.features should have size (2)
    //      rrc.allBundles should have size (1)
    //
    //      val validate = rrc.validate()
    //      validate should have length (1)
    //      validate.head should include("one bundle with startLevel '0'")
    //    }

    val resolver = FeatureResolver

    "should resolve to a valid config" in {
      val rcTry = RuntimeConfigCompanion.read(ConfigFactory.parseString(config))
      rcTry shouldBe a[Success[_]]

      val resolvedTry = resolver.resolve(rcTry.get, features)
      resolvedTry shouldBe a[Success[_]]
      val resolved = resolvedTry.get
      resolved.runtimeConfig.bundles should have size (1)
      resolved.allBundles should have size (4)
      resolved.runtimeConfig.features should have size (2)
    }

  }

  "Download" - {
    1.to(10).foreach { i =>
      s"should download a local file (try ${i})" in {
        withTestFiles("content", "") { (file, target) =>
          assert(Source.fromFile(file).getLines().toList === List("content"), "Precondition failed")
          val result = RuntimeConfigCompanion.download(file.toURI().toString(), target)
          assert(result === Success(target))
          assert(Source.fromFile(target).getLines().toList === List("content"))
        }
      }
    }
  }

}