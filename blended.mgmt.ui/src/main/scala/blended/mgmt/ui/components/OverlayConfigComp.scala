package blended.mgmt.ui.components

import blended.mgmt.ui.backend.DataManager
import blended.mgmt.ui.components.filter.And

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.vdom.prefix_<^._

import blended.mgmt.ui.ConsoleSettings
import blended.mgmt.ui.backend.Observer
import blended.mgmt.ui.components.filter.Filter
import blended.mgmt.ui.util.Logger
import blended.mgmt.ui.util.I18n
import blended.updater.config.RuntimeConfig
import blended.updater.config.OverlayConfig

object OverlayConfigComp {

  private[this] val log: Logger = Logger[OverlayConfigComp.type]
  private[this] val i18n = I18n()

  case class State(profiles: List[OverlayConfig], filter: And[OverlayConfig] = And(), selected: Option[OverlayConfig] = None) {
    def filteredOverlayConfigs: List[OverlayConfig] = profiles.filter(c => filter.matches(c))
    def consistent = this.copy(selected = selected.filter(s => profiles.filter(c => filter.matches(c)).exists(_ == s)))
  }

  class Backend(scope: BackendScope[Unit, State]) extends Observer[List[OverlayConfig]] {

    override def update(newData: List[OverlayConfig]): Unit = scope.setState(State(newData).consistent).runNow()

    def addFilter(filter: Filter[OverlayConfig]) = {
      log.debug("addFilter called with filter: " + filter + ", current state: " + scope.state.runNow())
      scope.modState(s => s.copy(filter = s.filter.append(filter).normalized).consistent).runNow()
    }

    def removeFilter(filter: Filter[OverlayConfig]) = {
      scope.modState(s => s.copy(filter = And((s.filter.filters.filter(_ != filter)): _*)).consistent).runNow()
    }

    def removeAllFilter() = {
      scope.modState(s => s.copy(filter = And()).consistent).runNow()
    }

    def selectContainer(profile: Option[OverlayConfig]): Callback = {
      scope.modState(s => s.copy(selected = profile).consistent)
    }

    def render(s: State) = {
      log.debug(s"Rerendering with state $s")

      
      val renderedOverlays = s.filteredOverlayConfigs.map { p =>
        <.div(
          <.span(
            ^.onClick --> selectContainer(Some(p)),
            p.name,
            "-",
            p.version
          )
        )
      }

      <.div(
        ^.`class` := "row",
        <.div(renderedOverlays: _*),
        <.div(
          OverlayConfigDetailComp.Component(OverlayConfigDetailComp.Props(s.selected)))
      )
    }
  }

  val Component =
    ReactComponentB[Unit]("OverlayConfig").
      initialState(State(profiles = List()))
      .renderBackend[Backend]
      .componentDidMount(c => DataManager.overlayConfigsData.addObserver(c.backend))
      .componentWillUnmount(c => DataManager.overlayConfigsData.removeObserver(c.backend))
      .build
}