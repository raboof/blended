package blended.mgmt.ui.components.filter

import blended.mgmt.ui.util.Logger
import blended.updater.config.RuntimeConfig

object RuntimeConfigFilter {

  private[this] val log = Logger[RuntimeConfigFilter.type]

  case class Name(name: String, exact: Boolean = false) extends Filter[RuntimeConfig] {
    override def matches(profile: RuntimeConfig): Boolean =
      if (exact) profile.name == name
      else profile.name.contains(name)
  }

  case class Version(version: String, exact: Boolean = false) extends Filter[RuntimeConfig] {
    override def matches(profile: RuntimeConfig): Boolean =
      if (exact) profile.version == version
      else profile.version.contains(version)
  }

}