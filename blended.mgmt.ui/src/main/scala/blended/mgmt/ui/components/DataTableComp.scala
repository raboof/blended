package blended.mgmt.ui.components

import blended.mgmt.ui.util.{I18n, Logger}
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object DataTableContent {
  def apply(title : String, content: Map[String, String]) : DataTableContent = DataTableContent(
    title = title,
    headings = Array("Name", "Value"),
    content = content.map{ case (k,v) => Array(k,v) }.toList
  )
}

case class DataTableContent(
  title : String,
  headings : Array[String],
  content : List[Array[String]]
)

object DataTableComp {

  private[this] val log = Logger[DataTableComp.type]
  private[this] val i18n = I18n()

  class Backend(scope: BackendScope[DataTableContent, Unit]) {

    def line(items : Array[String]) = <.tr(
      items.map(i => <.td(i18n.tr(i))):_*
    )

    def render(props: DataTableContent) = {
      <.div(
        ^.cls := "panel panel-default",
        <.div(
          ^.cls := "panel-heading",
          i18n.tr(props.title)
        ),
        <.table(
          ^.cls := "table",
          <.tr(
            props.headings.map(h => <.th(i18n.tr(h))):_*
          ),
          props.content.map(line)
        )
      )
    }
  }

  val Component = ReactComponentB[DataTableContent]("DataTable")
    .renderBackend[Backend]
    .build
}
