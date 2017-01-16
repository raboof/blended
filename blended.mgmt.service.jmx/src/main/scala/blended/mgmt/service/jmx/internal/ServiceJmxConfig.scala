package blended.mgmt.service.jmx.internal

import com.typesafe.config.{Config, ConfigObject}

import scala.collection.JavaConverters._

object ServiceJmxConfig {

  val intervalPath = "interval"
  val templatesPath = "templates"

  private def readTemplates(cfg: ConfigObject) : Map[String, ServiceTypeTemplate] = {
    cfg.unwrapped().asScala.map { case (key, value) =>
      ( key, ServiceTypeTemplate(key, cfg.toConfig().getObject(key).toConfig()) )
    }.toMap
  }

  def getStringMap(cfg: ConfigObject) : Map[String, String] =
    cfg.unwrapped().asScala.map { case (key, value) => (key, cfg.toConfig().getString(key)) }.toMap

  def apply(cfg: Config) : ServiceJmxConfig = new ServiceJmxConfig(

    interval = if (cfg.hasPath(intervalPath)) cfg.getInt(intervalPath) else 5,

    templates = if (cfg.hasPath(templatesPath)) readTemplates(cfg.getObject(templatesPath)) else Map.empty
  )
}



case class ServiceJmxConfig(
  interval : Int,
  templates : Map[String, ServiceTypeTemplate]
)

object ServiceTypeTemplate {

  val queryPath = "query"
  val attributesPath = "attributes"

  def apply(svcName: String, cfg: Config) : ServiceTypeTemplate = new ServiceTypeTemplate(
    name = svcName,
    domain = cfg.getString("domain"),
    query = if (cfg.hasPath(queryPath)) ServiceJmxConfig.getStringMap(cfg.getObject(queryPath)) else Map.empty,
    attributes = if (cfg.hasPath(attributesPath)) cfg.getStringList(attributesPath).asScala.toList else List.empty
  )
}

case class ServiceTypeTemplate(
  name : String,
  domain : String,
  query : Map[String, String],
  attributes : List[String]
)
