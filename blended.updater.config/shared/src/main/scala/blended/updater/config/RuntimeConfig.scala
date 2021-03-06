package blended.updater.config

import java.io.File
import java.net.URL

import scala.util.Try

case class RuntimeConfig(
  name: String,
  version: String,
  bundles: List[BundleConfig] = List.empty,
  startLevel: Int,
  defaultStartLevel: Int,
  properties: Map[String, String] = Map.empty,
  frameworkProperties: Map[String, String] = Map.empty,
  systemProperties: Map[String, String] = Map.empty,
  features: List[FeatureRef] = List.empty,
  resources: List[Artifact] = List.empty,
  resolvedFeatures: List[FeatureConfig] = List.empty) {

  override def toString(): String = s"${getClass().getSimpleName()}(name=${name},version=${version},bundles=${bundles}" +
    s",startLevel=${startLevel},defaultStartLevel=${defaultStartLevel},properties=${properties},frameworkProperties=${frameworkProperties}" +
    s",systemProperties=${systemProperties},features=${features},resources=${resources},resolvedFeatures=${resolvedFeatures})"

  def mvnBaseUrl: Option[String] = properties.get(RuntimeConfig.Properties.MVN_REPO)

  def resolveBundleUrl(url: String): Try[String] = RuntimeConfig.resolveBundleUrl(url, mvnBaseUrl)

  def resolveFileName(url: String): Try[String] = RuntimeConfig.resolveFileName(url)

  // TODO: Review this for JavaScript
  def baseDir(profileBaseDir: File): File = new File(profileBaseDir, s"${name}/${version}")

  //    def localRuntimeConfig(baseDir: File): LocalRuntimeConfig = LocalRuntimeConfig(runtimeConfig = this, baseDir = baseDir)

  /**
    * Try to create a [ResolvedRuntimeConfig]. This does not fetch missing [FeatureConfig]s.
    *
    * @see [FeatureResolver] for a way to resolve missing features.
    */
  def resolve(features: List[FeatureConfig] = List.empty): Try[ResolvedRuntimeConfig] = Try {
    ResolvedRuntimeConfig(this, features)
  }
}

object RuntimeConfig
  extends ((String, String, List[BundleConfig], Int, Int, Map[String, String], Map[String, String], Map[String, String], List[FeatureRef], List[Artifact], List[FeatureConfig]) => RuntimeConfig) {

  val MvnPrefix = "mvn:"

  object Properties {
    val PROFILES_BASE_DIR = "blended.updater.profiles.basedir"
    val PROFILE_DIR = "blended.updater.profile.dir"
    val PROFILE_NAME = "blended.updater.profile.name"
    val PROFILE_VERSION = "blended.updater.profile.version"
    /**
      * selected overlays, format: name:version,name:verion
      */
    val OVERLAYS = "blended.updater.profile.overlays"
    val PROFILE_LOOKUP_FILE = "blended.updater.profile.lookup.file"
    val MVN_REPO = "blended.updater.mvn.url"
    /** Comma separated list of properties required to be in the properties file */
    val PROFILE_PROPERTY_KEYS = "blended.updater.profile.properties.keys"
  }

  def resolveBundleUrl(url: String, mvnBaseUrl: Option[String] = None): Try[String] = Try {
    if (url.startsWith(MvnPrefix)) {
      mvnBaseUrl match {
        case Some(base) => MvnGav.parse(url.substring(MvnPrefix.size)).get.toUrl(base)
        case None => sys.error("No repository defined to resolve url: " + url)
      }
    } else url
  }

  def resolveFileName(url: String): Try[String] = Try {
    val resolvedUrl = if (url.startsWith(MvnPrefix)) {
      MvnGav.parse(url.substring(MvnPrefix.size)).get.toUrl("file:///")
    } else url
    val path = new URL(resolvedUrl).getPath()
    path.split("[/]").filter(!_.isEmpty()).reverse.headOption.getOrElse(path)
  }

}

