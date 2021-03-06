package blended.util.config

import scala.collection.JavaConverters._

import com.typesafe.config.Config

trait ConfigDefaultGetter extends ConfigAccessor {

  implicit class RichDefaultConfig(config: Config) {

    def getLong(key: String, default: Long): Long =
      if (config.hasPath(key)) {
        config.getLong(key)
      } else {
        default
      }

    def getAnyRef(key: String, default: AnyRef): AnyRef =
      if (config.hasPath(key)) {
        config.getAnyRef(key)
      } else {
        default
      }

    def getString(key: String, default: String): String =
      if (config.hasPath(key)) {
        config.getString(key)
      } else {
        default
      }

    def getInt(key: String, default: Int): Int =
      if (config.hasPath(key)) {
        config.getInt(key)
      } else {
        default
      }

    def getBoolean(key: String, default: Boolean): Boolean =
      if (config.hasPath(key)) {
        config.getBoolean(key)
      } else {
        default
      }

    def getStringList(key: String, default: List[String]): List[String] =
      if (config.hasPath(key)) {
        config.getStringList(key).asScala.toList
      } else {
        default
      }

    def getStringMap(key: String, default: Map[String, String]) : Map[String, String] =
      configStringMap(config, key).getOrElse(default)

    def getConfigMap(key: String, default: Map[String, Config]) : Map[String, Config] =
      configConfigMap(config, key).getOrElse(default)
  }
}
