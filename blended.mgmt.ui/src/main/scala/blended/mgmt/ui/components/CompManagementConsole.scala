package blended.mgmt.ui.components

import blended.mgmt.ui.backend.{ DataManager, Observer }
import blended.updater.config.ContainerInfo
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ BackendScope, ReactComponentB, Callback }
import blended.mgmt.ui.util.Logger
import blended.mgmt.ui.util.I18n
import blended.mgmt.ui.components.filter.Filter
import blended.mgmt.ui.components.filter.And

object CompManagementConsole {

  private[this] val log: Logger = Logger[CompManagementConsole.type]
  private[this] val i18n = I18n()

  case class State(containerList: List[ContainerInfo], filter: And[ContainerInfo] = And(), selected: Option[ContainerInfo] = None) {
    def filteredContainerList: List[ContainerInfo] = containerList.filter(c => filter.matches(c))
    def consistent = this.copy(selected = selected.filter(s => containerList.filter(c => filter.matches(c)).exists(_ == s)))
  }

  class Backend(scope: BackendScope[Unit, State]) extends Observer[List[ContainerInfo]] {

    override def update(newData: List[ContainerInfo]): Unit = scope.setState(State(newData).consistent).runNow()

    def addFilter(filter: Filter[ContainerInfo]) = {
      log.debug("addFilter called with filter: " + filter + ", current state: " + scope.state.runNow())
      scope.modState(s => s.copy(filter = s.filter.append(filter).normalized).consistent).runNow()
    }

    def removeFilter(filter: Filter[ContainerInfo]) = {
      scope.modState(s => s.copy(filter = And((s.filter.filters.filter(_ != filter)): _*)).consistent).runNow()
    }

    def removeAllFilter() = {
      scope.modState(s => s.copy(filter = And()).consistent).runNow()
    }

    def selectContainer(containerInfo: Option[ContainerInfo]) = {
      scope.modState(s => s.copy(selected = containerInfo).consistent).runNow()
    }

    def render(s: State) = {
      log.debug(s"Rerendering with $s")
      <.div(
        <.div(
          CompEditFilter.Component(CompEditFilter.Props(s.filter, s.containerList, addFilter))),
        <.div(
          CompViewFilter.Component(CompViewFilter.Props(s.filter, removeFilter, removeAllFilter))),
        <.div(
          CompContainerInfoList.Component(CompContainerInfoList.Props(s.filteredContainerList, selectContainer))),
        <.div(
          CompContainerDetail.Component(CompContainerDetail.Props(s.selected)))
      )
    }
  }

  val Component =
    ReactComponentB[Unit]("MgmtConsole")
      .initialState(State(containerList = List.empty))
      .renderBackend[Backend]
      .componentDidMount(c => DataManager.containerData.addObserver(c.backend))
      .componentWillUnmount(c => DataManager.containerData.removeObserver(c.backend))
      .build
}