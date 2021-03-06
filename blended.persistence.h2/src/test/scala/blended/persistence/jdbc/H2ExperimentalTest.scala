package blended.persistence.jdbc

import scala.collection.JavaConverters._

import blended.testsupport.TestFile
import blended.testsupport.scalatest.LoggingFreeSpec
import com.typesafe.config.ConfigFactory
import org.scalactic.source.Position.apply
import org.scalatest.Matchers

class H2ExperimentalTest extends LoggingFreeSpec with Matchers with TestFile {

  implicit val deletePolicy = TestFile.DeleteNever

  "TEST one insert" in {
    withTestDir() { dir =>
      DbFactory.withDataSource(dir, "db1") { dataSource =>
        val dao = new PersistedClassDao(dataSource)
        val txMgr = new DummyPlatformTransactionManager()
        dao.init()
        val exp = new PersistenceServiceJdbc(txMgr, dao)
        val testData = Map("name" -> "Luke", "surname" -> "Skywalker").asJava
        exp.persist("Person", testData)
        val actual = exp.findAll("Person")
        assert(actual.size === 1)
        assert(actual.toSet === Set(testData))
      }

    }
  }

  "TEST 2" in {

    withTestDir() { dir =>
      DbFactory.withDataSource(dir, "db2") { dataSource =>
        val dao = new PersistedClassDao(dataSource)
        val txMgr = new DummyPlatformTransactionManager()
        dao.init()
        val exp = new PersistenceServiceJdbc(txMgr, dao)
        val persisted = exp.persist("CLASS1", Map("key1" -> "value1").asJava)
        persisted.get("key1") should equal("value1")

        val found = exp.findAll("CLASS1")
        found should have size (1)
        val first = found.head
        first.get("key1") should equal("value1")

        val example = Map("key1" -> "value1").asJava
        val byExample = exp.findByExample("CLASS1", example)

        byExample should have size (1)
        byExample.head.get("key1") should equal("value1")

      }
    }
  }

  "Test 3 with complex Config Object" in {

    val config = ConfigFactory.parseString("""
        |key1=value1
        |key2=[ kv1, kv2 ]
        |key3 {
        |  k3a=v3a
        |  k3b=[ v3b1, v3b2 ]
        |}""".stripMargin)

    withTestDir(new java.io.File("target/tmp")) { dir =>
      config.root().unwrapped()
      DbFactory.withDataSource(dir, "db3") { dataSource =>
        val dao = new PersistedClassDao(dataSource)
        val txMgr = new DummyPlatformTransactionManager()
        dao.init()
        val exp = new PersistenceServiceJdbc(txMgr, dao)
        exp.persist("CONFIG", config.root().unwrapped())
        val loaded = exp.findAll("CONFIG")

        loaded should have size (1)

        loaded.head should equal(config.root().unwrapped())
      }
    }
  }

}