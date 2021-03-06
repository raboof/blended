package blended.updater

import blended.util.config.Implicits._
import com.typesafe.config.Config

object UpdaterConfig {

  val default: UpdaterConfig = {
    UpdaterConfig(
      artifactDownloaderPoolSize = 4,
      artifactCheckerPoolSize = 4,
      unpackerPoolSize = 4,
      autoStagingDelayMSec = 0,
      autoStagingIntervalMSec = 0,
      serviceInfoIntervalMSec = 0,
      serviceInfoLifetimeMSec = 0,
      mvnRepositories = List()
    )

  }

  def fromConfig(cfg: Config): UpdaterConfig = {
    UpdaterConfig(
      artifactDownloaderPoolSize = cfg.getInt("artifactDownloaderPoolSize", default.artifactDownloaderPoolSize),
      artifactCheckerPoolSize = cfg.getInt("artifactCheckerPoolSize", default.artifactCheckerPoolSize),
      unpackerPoolSize = cfg.getInt("updaterPoolSize", default.unpackerPoolSize),
      autoStagingDelayMSec = cfg.getLong("autoStagingDelayMSec", default.autoStagingDelayMSec),
      autoStagingIntervalMSec = cfg.getLong("autoStagingIntervalMSec", default.autoStagingIntervalMSec),
      serviceInfoIntervalMSec = cfg.getLong("serviceInfoIntervalMSec", default.serviceInfoIntervalMSec),
      serviceInfoLifetimeMSec = cfg.getLong("serviceInfoLifetimeMSec", default.serviceInfoLifetimeMSec),
      mvnRepositories = cfg.getStringList("mvnRepositories", List())
    )
  }
}

/**
 * Configuration for [Updater] actor.
 *
 * @param serviceInfoIntervalMSec Interval in milliseconds to publish a ServiceInfo message to the Akka event stream.
 *        An value of zero (0) or below indicates that no such information should be published.
 * @param serviceInfoLifetimeMSec The lifetime a serviceInfo message should be valid.
 */
case class UpdaterConfig(
  artifactDownloaderPoolSize: Int,
  artifactCheckerPoolSize: Int,
  unpackerPoolSize: Int,
  autoStagingDelayMSec: Long,
  autoStagingIntervalMSec: Long,
  serviceInfoIntervalMSec: Long,
  serviceInfoLifetimeMSec: Long,
  mvnRepositories: List[String]
)
