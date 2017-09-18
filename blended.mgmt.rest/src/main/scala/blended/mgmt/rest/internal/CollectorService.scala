package blended.mgmt.rest.internal

import akka.util.Timeout
import blended.spray.{ BlendedHttpRoute, SprayPrickleSupport }
import blended.updater.config._
import blended.updater.config.json.PrickleProtocol._
import org.slf4j.LoggerFactory
import spray.http.MediaTypes
import spray.routing.Route
import blended.security.spray.BlendedSecuredRoute

import scala.collection.immutable
import scala.concurrent.duration._
import spray.http.HttpHeader
import spray.http.HttpHeaders
import spray.http.AllOrigins

trait CollectorService
    extends BlendedHttpRoute
    with BlendedSecuredRoute {
  this: SprayPrickleSupport =>

  override val httpRoute: Route =
    collectorRoute ~
      infoRoute ~
      versionRoute ~
      runtimeConfigRoute ~
      overlayConfigRoute ~
      updateActionRoute ~
      rolloutProfileRoute

  private[this] val log = LoggerFactory.getLogger(classOf[CollectorService])

  def processContainerInfo(info: ContainerInfo): ContainerRegistryResponseOK

  def getCurrentState(): immutable.Seq[RemoteContainerState]

  /** Register a runtime config into the management container. */
  def registerRuntimeConfig(rc: RuntimeConfig): Unit

  /** Register a overlay config into the management container. */
  def registerOverlayConfig(oc: OverlayConfig): Unit

  /** Get all registered runtime configs of the management container. */
  def getRuntimeConfigs(): immutable.Seq[RuntimeConfig]

  /** Get all registered overlay configs of the managament container. */
  def getOverlayConfigs(): immutable.Seq[OverlayConfig]

  /** Promote (stage) an update action to a container. */
  def addUpdateAction(containerId: String, updateAction: UpdateAction): Unit

  def version: String

  def versionRoute: Route = {
    path("version") {
      get {
        complete {
          version
        }
      }
    }
  }

  def jsonReponse =
    respondWithHeader(HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins)) &
      respondWithMediaType(MediaTypes.`application/json`)

  def collectorRoute: Route = {

    implicit val timeout = Timeout(1.second)

    path("container") {
      post {
        entity(as[ContainerInfo]) { info =>
          log.debug("Processing container info: {}", info)
          val res = processContainerInfo(info)
          log.debug("Processing result: {}", res)
          complete(res)
        }
      }
    }
  }

  def infoRoute: Route = {
    path("container") {
      get {
        jsonReponse {
          complete {
            log.debug("About to provide container infos")
            val res = getCurrentState()
            log.debug("Result: {}", res)
            res
          }
        }
      }
    }
  }

  def runtimeConfigRoute: Route = {
    path("runtimeConfig") {
      get {
        jsonReponse {
          complete {
            getRuntimeConfigs()
          }
        }
      } ~
        post {
          requirePermission("profile:update") {
            entity(as[RuntimeConfig]) { rc =>
              registerRuntimeConfig(rc)
              complete(s"Registered ${rc.name}-${rc.version}")
            }
          }
        }
    }
  }

  def overlayConfigRoute: Route = {
    path("overlayConfig") {
      get {
        jsonReponse {
          complete {
            getOverlayConfigs()
          }
        }
      } ~
        post {
          requirePermission("profile:update") {
            entity(as[OverlayConfig]) { oc =>
              registerOverlayConfig(oc)
              complete(s"Registered ${oc.name}-${oc.version}")
            }
          }
        }
    }
  }

  def updateActionRoute: Route = {
    path("container" / Segment / "update") { containerId =>
      post {
        requirePermission("profile:update") {
          entity(as[UpdateAction]) { updateAction =>
            addUpdateAction(containerId, updateAction)
            complete(s"Added UpdateAction to ${containerId}")
          }
        }
      }
    }
  }

  def rolloutProfileRoute: Route = {
    path("rollout" / "profile") {
      post {
        requirePermission("profile:update") {
          entity(as[RolloutProfile]) { rolloutProfile =>
            complete {
              // TODO: check, if we already know the profile
              rolloutProfile.containerIds.foreach { containerId =>
                addUpdateAction(
                  containerId = containerId,
                  updateAction = StageProfile(
                    profileName = rolloutProfile.profileName,
                    profileVersion = rolloutProfile.profileVersion,
                    overlays = rolloutProfile.overlays))
              }
              s"Recorded ${rolloutProfile.containerIds.size} rollout actions"
            }
          }
        }
      }
    }

  }

}
