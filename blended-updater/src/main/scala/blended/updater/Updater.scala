package blended.updater

import java.io.File
import java.util.UUID
import scala.collection.immutable._
import org.osgi.framework.BundleContext
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.routing.BalancingPool
import blended.updater.BlockingDownloader.Download
import blended.updater.BlockingDownloader.DownloadFailed
import blended.updater.BlockingDownloader.DownloadFinished
import blended.updater.Sha1SumChecker.CheckFile
import blended.updater.Sha1SumChecker.InvalidChecksum
import blended.updater.Sha1SumChecker.ValidChecksum
import blended.updater.config.LauncherConfig
import com.typesafe.config.ConfigFactory
import scala.util.control.NonFatal
import blended.updater.config.RuntimeConfig
import blended.updater.config.ConfigConverter
import blended.updater.config.ConfigWriter

object Updater {

  sealed trait Protocol {
    def requestId: String
  }
  trait Reply

  /**
   * Request lists of runtime configurations. Replied with [RuntimeConfigs].
   */
  case class GetRuntimeConfigs(requestId: String) extends Protocol
  case class RuntimeConfigs(requestId: String, unstaged: Seq[RuntimeConfig], staged: Seq[RuntimeConfig]) extends Reply

  case class AddRuntimeConfig(requestId: String, runtimeConfig: RuntimeConfig) extends Protocol
  case class RuntimeConfigAdded(requestId: String) extends Reply
  case class RuntimeConfigAdditionFailed(requestId: String, reason: String) extends Reply

  case class ScanForRuntimeConfigs(requestId: String) extends Protocol

  // explicit trigger staging of a config, but idea is to automatically stage not already staged configs when idle
  case class StageRuntimeConfig(requestId: String, name: String, version: String) extends Protocol
  case class RuntimeConfigStaged(requestId: String) extends Reply
  case class RuntimeConfigStagingFailed(requestId: String, reason: String) extends Reply

  case class ActivateRuntimeConfig(requestId: String, name: String, version: String) extends Protocol
  case class RuntimeConfigActivated(requestId: String) extends Reply
  case class RuntimeConfigActivationFailed(requestId: String, reason: String) extends Reply

  case class GetProgress(requestId: String) extends Protocol
  case class Progress(requestId: String, progress: Int) extends Reply

  case class UnknownRuntimeConfig(requestId: String) extends Reply
  case class UnknownRequestId(requestId: String) extends Reply

  def props(
    configDir: String,
    baseDir: File,
    launcherConfigSetter: LauncherConfig => Unit,
    restartFramework: () => Unit,
    artifactDownloaderProps: Props = null,
    artifactCheckerProps: Props = null): Props =
    Props(new Updater(
      configDir,
      baseDir,
      launcherConfigSetter,
      restartFramework,
      Option(artifactDownloaderProps),
      Option(artifactCheckerProps)
    ))

  /**
   * A bundle in progress, e.g. downloading or verifying.
   */
  private case class BundleInProgress(reqId: String, bundle: RuntimeConfig.BundleConfig, file: File)

  /**
   * Internal working state of in-progress stagings.
   */
  private case class State(
      requestId: String,
      requestActor: ActorRef,
      config: RuntimeConfig,
      installDir: File,
      bundlesToDownload: Seq[BundleInProgress],
      bundlesToCheck: Seq[BundleInProgress]) {

    val profileId = ProfileId(config.name, config.version)

    def progress(): Int = {
      val allBundlesSize = config.bundles.size
      if (allBundlesSize > 0)
        (100 / allBundlesSize) * (allBundlesSize - bundlesToDownload.size - bundlesToCheck.size)
      else 100
    }

  }

  case class ProfileId(name: String, version: String)

  object Profile {
    sealed trait ProfileState
    case class Invalid(issues: Seq[String]) extends ProfileState
    case object Valid extends ProfileState
  }

  case class Profile(dir: File, config: RuntimeConfig, state: Option[Profile.ProfileState]) {
    def profile: ProfileId = ProfileId(config.name, config.version)
  }

}

class Updater(
  configDir: String,
  installBaseDir: File,
  launchConfigSetter: LauncherConfig => Unit,
  restartFramework: () => Unit,
  artifactDownloaderProps: Option[Props],
  artifactCheckerProps: Option[Props])
    extends Actor
    with ActorLogging {
  import Updater._

  override def preStart(): Unit = {
    self ! ScanForRuntimeConfigs(UUID.randomUUID().toString())
  }

  val artifactDownloader = context.actorOf(
    artifactDownloaderProps.getOrElse(BalancingPool(4).props(BlockingDownloader.props())),
    "artifactDownloader")
  val artifactChecker = context.actorOf(
    artifactCheckerProps.getOrElse(BalancingPool(4).props(Sha1SumChecker.props())),
    "artifactChecker")

  private[this] var stagingInProgress: Map[String, State] = Map()

  private[this] var profiles: Map[ProfileId, Profile] = Map()

  private[this] def stageInProgress(state: State): Unit = {
    val id = state.requestId
    val config = state.config
    val progress = state.progress()
    log.debug("Progress: {} for reqestId: {}", progress, id)

    if (state.bundlesToCheck.isEmpty && state.bundlesToDownload.isEmpty) {
      stagingInProgress = stagingInProgress.filterKeys(id != _)
      profiles += state.profileId -> Profile(state.installDir, state.config, Some(Profile.Valid))
      state.requestActor ! RuntimeConfigStaged(id)
    } else {
      stagingInProgress += id -> state
    }
  }

  def findConfig(id: ProfileId): Option[RuntimeConfig] = profiles.get(id).map(_.config)

  private[this] def nextId(): String = UUID.randomUUID().toString()

  def protocol(msg: Protocol): Unit = msg match {

    case ScanForRuntimeConfigs(reqId) =>
      val nameDirs = Option(installBaseDir.listFiles).getOrElse(Array()).toList
      val foundProfiles = nameDirs.flatMap { nameDir =>
        val versionDirs = Option(nameDir.listFiles).getOrElse(Array()).toList
        versionDirs.flatMap { versionDir =>
          val profileFile = new File(versionDir, "profile.conf")
          if (!profileFile.exists()) Nil
          else {
            try {
              val config = ConfigFactory.parseFile(profileFile).resolve()
              val runtimeConfig = RuntimeConfig.read(config)
              if (runtimeConfig.name == nameDir.getName() && runtimeConfig.version == versionDir.getName()) {
                val stagedMarkerFile = new File(versionDir, ".staged")
                val profileState =
                  if (stagedMarkerFile.exists() && stagedMarkerFile.lastModified() >= profileFile.lastModified()) {
                    Profile.Valid
                  } else {
                    Profile.Invalid(Seq())
                  }
                List(Profile(versionDir, runtimeConfig, Some(profileState)))
              } else List()
            } catch {
              case NonFatal(e) => List()
            }
          }
        }

      }
      profiles = foundProfiles.map { profile => profile.profile -> profile }.toMap

    case GetRuntimeConfigs(reqId) =>
      val (staged, unstaged) = profiles.values.toList.partition { p =>
        p.state match {
          case Some(Profile.Valid) => true
          case _ => false
        }
      }
      sender() ! RuntimeConfigs(reqId,
        unstaged = unstaged.map(_.config),
        staged = staged.map(_.config)
      )

    case AddRuntimeConfig(reqId, config) =>
      val id = ProfileId(config.name, config.version)
      findConfig(id) match {
        case None =>
          // TODO: stage

          val dir = new File(new File(installBaseDir, config.name), config.version)
          dir.mkdirs()

          val confFile = new File(dir, "profile.conf")

          ConfigWriter.write(RuntimeConfig.toConfig(config), confFile, None)
          profiles += id -> Profile(dir, config, None)

          sender() ! RuntimeConfigAdded(reqId)
        case Some(`config`) =>
          sender() ! RuntimeConfigAdded(reqId)
        case Some(collision) =>
          sender() ! RuntimeConfigAdditionFailed(reqId, "A different runtime config is already present under the same coordinates")
      }

    case StageRuntimeConfig(reqId, name, version) =>
      profiles.get(ProfileId(name, version)) match {
        case None =>
          sender() ! RuntimeConfigStagingFailed(reqId, "No such runtime configuration found")

        case Some(Profile(dir, config, Some(Profile.Valid))) =>
          // already staged
          sender() ! RuntimeConfigStaged(reqId)

        case Some(Profile(installDir, config, stateOption)) =>
          val reqActor = sender()
          if (stagingInProgress.contains(reqId)) {
            log.error("Duplicate id detected. Dropping request: {}", msg)
          } else {
            log.info("About to stage installation: {}", config)

            // analyze config
            val bundles = config.framework :: config.bundles.toList
            // determine missing artifacts
            val (existing, missing) = bundles.partition(b => new File(installDir, b.jarName).exists())
            // download artifacts
            val missingWithId = missing.map { missingBundle =>
              val inProgress = BundleInProgress(nextId(), missingBundle, new File(installDir, missingBundle.jarName))
              artifactDownloader ! Download(inProgress.reqId, self, missingBundle.url, inProgress.file)
              inProgress
            }
            // check artifacts
            val existingWithId = existing.map { existingBundle =>
              val inProgress = BundleInProgress(nextId(), existingBundle, new File(installDir, existingBundle.jarName))
              artifactChecker ! CheckFile(inProgress.reqId, self, inProgress.file, existingBundle.sha1Sum)
              inProgress
            }
            stageInProgress(State(reqId, reqActor, config, installDir, missingWithId, existingWithId))
          }

      }

    case ActivateRuntimeConfig(reqId, name: String, version: String) =>
      val requestingActor = sender()
      profiles.get(ProfileId(name, version)) match {
        case Some(Profile(dir, config, Some(Profile.Valid))) =>
          // write config
          val launcherConfig = ConfigConverter.runtimeConfigToLauncherConfig(config, installBaseDir.getPath())
          log.debug("About to activate launcher config: {}", launcherConfig)
          launchConfigSetter(launcherConfig)
          requestingActor ! RuntimeConfigActivated(reqId)
          restartFramework()
        case _ =>
          sender() ! RuntimeConfigActivationFailed(reqId, "No such active runtime configuration found")
      }

    case GetProgress(reqId) =>
      stagingInProgress.get(reqId) match {
        case Some(state) => sender ! Progress(reqId, state.progress())
        case None => sender() ! UnknownRequestId(reqId)
      }

  }

  override def receive: Actor.Receive = LoggingReceive {
    case p: Protocol => protocol(p)

    case DownloadFinished(downloadId, url, file) =>
      val foundProgress = stagingInProgress.values.flatMap { state =>
        state.bundlesToDownload.find { bip => bip.reqId == downloadId }.map(state -> _).toList
      }.toList
      foundProgress match {
        case Nil =>
          log.error("Unkown download id {}. Url: {}", downloadId, url)
        case (state, bundleInProgress) :: _ =>
          val newToCheck = bundleInProgress.copy(reqId = nextId())
          artifactChecker ! CheckFile(newToCheck.reqId, self, newToCheck.file, newToCheck.bundle.sha1Sum)
          stageInProgress(state.copy(
            bundlesToDownload = state.bundlesToDownload.filter(bundleInProgress != _),
            bundlesToCheck = newToCheck +: state.bundlesToCheck
          ))
      }

    case DownloadFailed(downloadId, url, file, error) =>
      val foundState = stagingInProgress.values.find { state =>
        state.bundlesToDownload.find { bip => bip.reqId == downloadId }.isDefined
      }
      foundState match {
        case None =>
          log.error("Unkown download id {}. Url: {}", downloadId, url)
        case Some(state) =>
          log.debug("Cancelling in progress state: {}\nReason: {}", state, error)
          stagingInProgress = stagingInProgress.filterKeys(state.requestId != _)
          state.requestActor ! RuntimeConfigStagingFailed(state.requestId, error.getMessage())
      }

    case ValidChecksum(checkId, file, sha1Sum) =>
      val foundProgress = stagingInProgress.values.flatMap { state =>
        state.bundlesToCheck.find { bip => bip.reqId == checkId }.map(state -> _).toList
      }.toList
      foundProgress match {
        case Nil =>
          log.error("Unkown check id {}. file: {}", checkId, file)
        case (state, bundleInProgress) :: _ =>
          stageInProgress(state.copy(
            bundlesToCheck = state.bundlesToCheck.filter(bundleInProgress != _)
          ))
      }

    case InvalidChecksum(checkId, file, sha1Sum) =>
      val foundProgress = stagingInProgress.values.flatMap { state =>
        state.bundlesToCheck.find { bip => bip.reqId == checkId }.map(state -> _).toList
      }.toList
      foundProgress match {
        case Nil =>
          log.error("Unkown check id {}. file: {}", checkId, file)
        case (state, bundleInProgress) :: _ =>
          val errorMsg = "Invalid checksum for resource from URL: " + bundleInProgress.bundle.url
          log.debug("Cancelling in progress state: {}\nReason: Invalid checksum", state)
          stagingInProgress = stagingInProgress.filterKeys(state.requestId != _)
          state.requestActor ! RuntimeConfigStagingFailed(state.requestId, errorMsg)
      }

  }

}
