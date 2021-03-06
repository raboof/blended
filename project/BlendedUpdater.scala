import sbt._

object BlendedUpdater extends ProjectFactory {

  private[this] val helper = new ProjectSettings(
    projectName = "blended.updater",
    description = "OSGi Updater",
    deps = Seq(
      Dependencies.orgOsgi,
      Dependencies.domino,
      Dependencies.akkaOsgi,
      Dependencies.slf4j,
      Dependencies.typesafeConfig,
      Dependencies.akkaTestkit % "test",
      Dependencies.scalatest % "test",
      Dependencies.felixFramework % "test",
      Dependencies.logbackClassic % "test",
      Dependencies.akkaSlf4j % "test",
      Dependencies.felixGogoRuntime % "test",
      Dependencies.felixGogoShell % "test",
      Dependencies.felixGogoCommand % "test",
      Dependencies.felixFileinstall % "test",
      Dependencies.mockitoAll % "test"
    ),
    adaptBundle = b => b.copy(
      bundleActivator = s"${b.bundleSymbolicName}.internal.BlendedUpdaterActivator",
      importPackage = Seq("blended.launcher.runtime;resolution:=optional")

    )
  )

  override val project = helper.baseProject.dependsOn(
    BlendedUpdaterConfigJvm.project,
    BlendedLauncher.project,
    BlendedMgmtBase.project,
    BlendedContainerContextApi.project,
    BlendedAkka.project,
    BlendedTestsupport.project % "test"
  )
}
