package blended.updater.remote.internal

import blended.updater.remote.RemoteUpdater
import java.io.File

import blended.updater.config._
import com.typesafe.config.ConfigFactory
import blended.updater.remote.ContainerState
import java.util.Date

class RemoteCommands(remoteUpdater: RemoteUpdater) {

  def commands = Seq(
    "remoteShow" -> "Show update information about remote container",
    "remoteStage" -> "Stage a profile for a remote container",
    "remoteActivate" -> "Activate a profile for a remote container",
    "registerProfile" -> "Register a profile",
    "registerOverlay" -> "Register an overlay",
    "profiles" -> "Show all registered profiles",
    "overlays" -> "Show all registered overlays"
  )

  def renderContainerState(state: ContainerState): String = {
    s"""Container ID: ${state.containerId}
        |  profiles: ${state.profiles.mkString(", ")}
        |  outstanding actions: ${
      state.outstandingActions.map {
        // TODO: overlays
        case AddRuntimeConfig(rc) => s"add runtime config ${rc.name}-${rc.version}"
        case AddOverlayConfig(oc) => s"add overlay config ${oc.name}-${oc.version}"
        case StageProfile(n, v, o) => s"stage ${n}-${v} with ${o.toList.sorted.mkString(" and ")}"
        case ActivateProfile(n, v, o) => s"activate ${n}-${v} with ${o.toList.sorted.mkString(" and ")}"
      }.mkString(", ")
    }
        |  last sync: ${state.syncTimeStamp.map(s => new Date(s)).mkString}""".stripMargin
  }

  def remoteShow(): String = {
    remoteUpdater.getContainerIds().map { id =>
      s"Update state of container with ID ${id}:\n${remoteUpdater.getContainerState(id).map(renderContainerState)}\n"
    }.mkString("\n")
  }

  def remoteShow(containerId: String): String = {
    remoteUpdater.getContainerState(containerId) match {
      case Some(state) => s"Update state of container with ID ${containerId}:\n${state}\n"
      case None => s"Unknown container ID: ${containerId}"
    }
  }

  def profiles(): String = {
    remoteUpdater.getRuntimeConfigs().map(rc => s"${rc.name}-${rc.version}").mkString("\n")
  }

  def overlays(): String = {
    remoteUpdater.getOverlayConfigs().map(oc => s"${oc.name}-${oc.version}").mkString("\n")
  }

  def registerProfile(profileFile: String): Unit = {
    val file = new File(profileFile)
    if (!file.exists()) {
      println(s"File ${file.toURI()} does not exist")
    } else {
      println(s"Reading profile from file: ${file.toURI()}")
      val config = ConfigFactory.parseFile(file).resolve()
      val runtimeConfig = RuntimeConfigCompanion.read(config)
      println(s"Profile: ${runtimeConfig}")
      remoteUpdater.registerRuntimeConfig(runtimeConfig.get)
    }
  }

  def registerOverlay(overlayFile : String) : Unit = {
    val file = new File(overlayFile)
    if (!file.exists()) {
      println(s"File ${file.toURI()} does not exist")
    } else {
      println(s"Reading overlay from file: ${file.toURI()}")
      val config = ConfigFactory.parseFile(file).resolve()
      val overlayConfig = OverlayConfigCompanion.read(config)
      println(s"Overlay: ${overlayConfig}")
      remoteUpdater.registerOverlayConfig(overlayConfig.get)
    }
  }

  def remoteStage(containerId: String, profileName: String, profileVersion: String): Unit = {
    remoteUpdater.getRuntimeConfigs().find(rc => rc.name == profileName && rc.version == profileVersion) match {
      case None => println(s"Profile '${profileName}-${profileVersion}' not found")
      case Some(rc) =>
        // FIXME: support for overlay
        remoteUpdater.addAction(containerId, AddRuntimeConfig(rc))
        remoteUpdater.addAction(containerId, StageProfile(profileName, profileVersion, List.empty))
        println(s"Scheduled profile staging for container with ID ${containerId}. Config: ${profileName}-${profileVersion}")
    }
  }

  def remoteActivate(containerId: String, profileName: String, profileVersion: String): Unit = {
    // FIXME: support for overlays
    remoteUpdater.addAction(containerId, ActivateProfile(profileName, profileVersion, List.empty))
    println(s"Scheduled profile activation for container with ID ${containerId}. Profile: ${profileName}-${profileVersion}")
  }

}