package blended.testsupport.pojosr

import java.io.File
import java.util.Properties

import blended.container.context.api.ContainerContext
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import com.typesafe.config.ConfigObject
import com.typesafe.config.impl.Parseable

class MockContainerContext(baseDir: String) extends ContainerContext {

  override def getContainerDirectory(): String = baseDir

  override def getContainerConfigDirectory(): String = getContainerDirectory() + "/etc"

  override def getContainerLogDirectory(): String = baseDir

  override def getProfileDirectory(): String = getContainerDirectory()

  override def getProfileConfigDirectory(): String = getContainerConfigDirectory()

  override def getContainerHostname(): String = "localhost"

  private def getSystemProperties(): Properties = {
    // Avoid ConcurrentModificationException due to parallel setting of system properties by copying properties
    val systemProperties     = System.getProperties()
    val systemPropertiesCopy = new Properties()
    systemPropertiesCopy.putAll(systemProperties)
    systemPropertiesCopy
  }

  private def loadSystemProperties(): ConfigObject = {
    Parseable
      .newProperties(
        getSystemProperties(),
        ConfigParseOptions.defaults().setOriginDescription("system properties")
      )
      .parse()
  }

  override def getContainerConfig(): Config = {
    val sysProps = loadSystemProperties()
    val envProps = ConfigFactory.systemEnvironment()

    ConfigFactory
      .parseFile(
        new File(getProfileConfigDirectory(), "application.conf"),
        ConfigParseOptions.defaults().setAllowMissing(false)
      )
      .withFallback(sysProps)
      .withFallback(envProps)
      .resolve()
  }
}
