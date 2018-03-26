package blended.testsupport.pojosr

import java.io.File

import blended.container.context.api.ContainerContext
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}

class MockContainerContext(baseDir: String) extends ContainerContext {

  override def getContainerDirectory(): String = baseDir

  override def getContainerConfigDirectory(): String = getContainerDirectory() + "/etc"

  override def getContainerLogDirectory(): String = baseDir

  override def getProfileDirectory(): String = getContainerDirectory()

  override def getProfileConfigDirectory(): String = getContainerConfigDirectory()

  override def getContainerHostname(): String = "localhost"

  override def getContainerConfig(): Config = {
    val sysProps = ConfigFactory.systemProperties()
    val envProps = ConfigFactory.systemEnvironment()

    ConfigFactory.parseFile(
      new File(getProfileConfigDirectory(), "application.conf"),
      ConfigParseOptions.defaults().setAllowMissing(false)
    ).withFallback(sysProps).withFallback(envProps).resolve()
  }
}