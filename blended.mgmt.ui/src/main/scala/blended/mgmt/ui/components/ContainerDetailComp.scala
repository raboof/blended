package blended.mgmt.ui.components

import blended.mgmt.ui.backend.ProfileUpdater
import blended.mgmt.ui.util.{I18n, Logger}
import blended.updater.config.Profile.SingleProfile
import blended.updater.config._
import japgolly.scalajs.react._
import vdom.html_<^._

/**
 * React component to render details of a [[ContainerInfo]].
 */
object ContainerDetailComp {

  private[this] val log = Logger[ContainerDetailComp.type]
  private[this] val i18n = I18n()

  case class Props(containerInfo: Option[ContainerInfo] = None, profileUpdater: Option[ProfileUpdater] = None)

  class Backend(scope: BackendScope[Props, Unit]) {

    def sendUpdateAction(updateActions: UpdateAction*)(event: ReactEvent) = {
      scope.props.map { p =>
        p.containerInfo match {
          case Some(ci) =>
            p.profileUpdater match {
              case Some(pu) =>
                pu.addUpdateActions(ci.containerId, updateActions.toList)
              case None =>
                log.info(s"Skipping update action. No profile updater set")
            }
          case None =>
            log.info(s"Skipping update action. No containerInfo set")
        }
      }
    }

    def activateProfile(profile: SingleProfile)(event: ReactEvent) = {
      sendUpdateAction(ActivateProfile(profile.name, profile.version, profile.overlays))(event)
    }

    def resolveProfile(profile: SingleProfile)(event: ReactEvent) = {
      sendUpdateAction(StageProfile(profile.name, profile.version, profile.overlays))(event)
    }

    def deleteProfile(profile: SingleProfile)(event: ReactEvent) = {
      CallbackTo {
        log.trace(s"Unimplemented callback: delete profile ${profile}")
      }
    }

    def render(props: Props) = {
      props match {
        case Props(None, _) => <.span(i18n.tr("No Container selected"))
        case Props(Some(containerInfo), profileUpdater) =>

          val props = DataTableComp.Component(DataTableContent(title = "Container Properties", content = containerInfo.properties))
            //containerInfo.properties.map(p => <.div(<.span("  ", p._1, ": "), <.span(p._2))).toSeq

          val profiles = containerInfo.profiles.flatMap(_.toSingle).map { profile =>

            val genTitle = if (profile.overlays.isEmpty) i18n.tr("without overlays") else profile.overlays.mkString(", ")

            <.div(
              ^.`class` := profile.state.toString,
              s"${profile.name}-${profile.version} ${genTitle} ",
              <.span(
                (^.title := s"${profile.state}: ${profile.overlaySet.reason.getOrElse("")}").when(profile.overlaySet.reason.isDefined),
                s"(${profile.state})"
              ),
              " ",
              <.button(
                ^.`type` := "button",
                ^.`class` := "btn btn-default btn-xs",
                (^.disabled := true).when(profileUpdater.isEmpty || profile.state != OverlayState.Valid),
                ^.onClick ==> activateProfile(profile),
                i18n.tr("Activate")
              ).when(profile.state != OverlayState.Active),
              " ",
              <.button(
                ^.`type` := "button",
                ^.`class` := "btn btn-default btn-xs",
                (^.disabled := true).when(profileUpdater.isEmpty),
                ^.onClick ==> deleteProfile(profile),
                i18n.tr("Delete")
              ).when(profile.state != OverlayState.Active),
              " ",
              <.button(
                ^.`type` := "button",
                ^.`class` := "btn btn-default btn-xs",
                (^.disabled := true).when(profileUpdater.isEmpty),
                ^.onClick ==> resolveProfile(profile),
                i18n.tr("Try to Resolve")
              ).when(profile.state == OverlayState.Invalid)
            )
          }

          val services = containerInfo.serviceInfos.map { info => <.div(ServiceInfoComp.Component(info)) }

          <.div(
            <.h2(
              i18n.tr("Container ID:"),
              " ",
              containerInfo.containerId
            ),
            props,
            <.div(
              ^.cls := "panel panel-default",
              <.div(
                ^.cls := "panel-heading",
                i18n.tr("Profiles:")
              ),
              <.div(profiles: _*)
            ),
            <.div(
              ^.cls := "panel panel-default",
              <.h2(i18n.tr("Container Services:")),
              <.div(services: _*)
            )
          )

      }
    }
  }

  val Component = ScalaComponent.builder[Props]("ContainerDetail")
    .renderBackend[Backend]
    .build
}