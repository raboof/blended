package blended.jms.utils

import blended.container.context.api.ContainerIdentifierService
import blended.jms.utils.ConnectionFactoryActivator.{CF_JNDI_NAME, DEFAULT_PWD, DEFAULT_USER, USE_JNDI}
import blended.updater.config.util.ConfigPropertyMapConverter
import com.typesafe.config.Config
import blended.util.config.Implicits._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object BlendedJMSConnectionConfig {

  val defaultConfig = BlendedJMSConnectionConfig(
    vendor = "",
    provider = "",
    enabled = true,
    jmxEnabled = true,
    pingEnabled = true,
    pingTolerance = 5,
    pingInterval = 30,
    pingTimeout = 3000,
    retryInterval = 5,
    minReconnect = 300,
    maxReconnectTimeout = -1,
    clientId = "$[[" + ContainerIdentifierService.containerId + "]]",
    defaultUser = None,
    defaultPassword  = None,
    pingDestination = "topic:blended.ping",
    properties = Map.empty,
    useJndi = false,
    jndiName = None,
    cfEnabled = None,
    cfClassName = None,
    ctxtClassName = None,
    jmsClassloader  = None
  )

  def fromConfig(stringResolver : String => Try[String])(vendor: String, provider: String, cfg: Config) : BlendedJMSConnectionConfig = {

    val prov = cfg.getString("provider", provider)
    val enabled = cfg.getBoolean("enabled", defaultConfig.enabled)
    val jmxEnabled = cfg.getBoolean("jmxEnabled", defaultConfig.jmxEnabled)
    val pingEnabled = cfg.getBoolean("pingEnabled", defaultConfig.pingEnabled)
    val pingTolerance = cfg.getInt("pingTolerance", defaultConfig.pingTolerance)
    val pingInterval = cfg.getInt("pingInterval", defaultConfig.pingInterval)
    val pingTimeout = cfg.getInt("pingTimeout", defaultConfig.pingTimeout)
    val retryInterval = cfg.getInt("retryInterval", defaultConfig.retryInterval)
    val minReconnect = cfg.getInt("minReconnect", defaultConfig.minReconnect)
    val maxReconnectTimeout = cfg.getInt("maxReconnectTimeout", defaultConfig.maxReconnectTimeout)

    val clientId = if (cfg.hasPath("clientId"))
      stringResolver(cfg.getString("clientId")) match {
        case Failure(t) => throw t
        case Success(id) => id
      }
    else
      defaultConfig.clientId

    val defaultUser = cfg.getStringOption(DEFAULT_USER)
    val defaultPasswd = cfg.getStringOption(DEFAULT_PWD)
    val destination = cfg.getString("destination", defaultConfig.pingDestination)

    val properties : Map[String, String] =
      ConfigPropertyMapConverter.getKeyAsPropertyMap(cfg,"properties", Option(() => defaultConfig.properties))
      .mapValues { v =>
          stringResolver(v) match {
            case Failure(t) => throw t
            case Success(s) => s
          }
      }

    val jndiName = cfg.getStringOption(CF_JNDI_NAME)
    val useJndi = cfg.getBoolean(USE_JNDI, defaultConfig.useJndi)

    BlendedJMSConnectionConfig(
      vendor = vendor,
      enabled = enabled,
      provider = prov,
      jmxEnabled = jmxEnabled,
      pingEnabled = pingEnabled,
      pingTolerance = pingTolerance,
      pingInterval = pingInterval,
      pingTimeout = pingTimeout,
      retryInterval = retryInterval,
      minReconnect = minReconnect,
      maxReconnectTimeout = maxReconnectTimeout,
      clientId = clientId,
      defaultUser = defaultUser,
      defaultPassword = defaultPasswd,
      pingDestination = destination,
      properties = properties,
      jndiName = jndiName,
      useJndi = useJndi,
      cfEnabled = None,
      jmsClassloader = defaultConfig.jmsClassloader,
      ctxtClassName = defaultConfig.ctxtClassName,
      cfClassName = defaultConfig.cfClassName
    )
  }
}

case class BlendedJMSConnectionConfig(
  vendor : String,
  provider : String,
  enabled : Boolean,
  jmxEnabled : Boolean,
  pingEnabled : Boolean,
  pingTolerance : Int,
  pingInterval : Int,
  pingTimeout : Int,
  retryInterval : Int,
  minReconnect : Int,
  maxReconnectTimeout: Int,
  clientId : String,
  defaultUser : Option[String],
  defaultPassword : Option[String],
  pingDestination : String,
  properties : Map[String, String],
  useJndi : Boolean,
  jndiName : Option[String] = None,
  cfEnabled : Option[BlendedJMSConnectionConfig => Boolean],
  cfClassName: Option[String],
  ctxtClassName : Option[String],
  jmsClassloader : Option[ClassLoader]
)
