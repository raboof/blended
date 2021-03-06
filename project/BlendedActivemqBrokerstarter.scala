import sbt._

object BlendedActivemqBrokerstarter extends ProjectFactory {

  private[this] val helper = new ProjectSettings(
    projectName = "blended.activemq.brokerstarter",
    description = "A simple wrapper around an Active MQ broker that makes sure that the broker is completely started before exposing a connection factory OSGi service",
    deps = Seq(
      Dependencies.camelJms,
      Dependencies.activeMqBroker,
      Dependencies.activeMqSpring
    )
  )

  override val project = helper.baseProject.dependsOn(
    BlendedAkka.project,
    BlendedJmsUtils.project
  )
}
