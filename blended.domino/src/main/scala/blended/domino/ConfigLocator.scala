package blended.domino

import java.io.File

import blended.container.context.api.ContainerContext
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

class ConfigLocator(ctContext: ContainerContext) {

  private[this] val log = LoggerFactory.getLogger(classOf[ConfigLocator])

  private[this] val sysProps = ConfigFactory.systemProperties()
  private[this] val envProps = ConfigFactory.systemEnvironment()

  private[this] def config(fileName : String) : Config = {
    val file = new File(ctContext.getProfileConfigDirectory(), fileName)
    log.debug("Retrieving config from [{}]", file.getAbsolutePath())

    if (file.exists && file.isFile && file.canRead)
      ConfigFactory.parseFile(file).withFallback(sysProps).withFallback(envProps).resolve()
    else
      ConfigFactory.empty()
  }

  def getConfig(id: String): Config = config(s"$id.conf") match {
    case empty if empty.isEmpty =>

      val cfg = ctContext.getContainerConfig()
      if (cfg.hasPath(id)) cfg.getConfig(id) else ConfigFactory.empty()

    case cfg => cfg
  }
}
