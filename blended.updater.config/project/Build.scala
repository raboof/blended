import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object Build extends sbt.Build {

  val appName = "blended.updater.config"

  lazy val root =
    project.in(file("."))
      .settings(projectSettings: _*)
      .enablePlugins(ScalaJSPlugin)

  lazy val projectSettings = Seq(
    organization := "de.wayofquality.blended",
    version := BlendedVersions.blendedVersion,
    name := appName,
    scalaVersion := BlendedVersions.scalaVersion,
    (artifact in packageBin) ~= { a : Artifact => a.copy(name = appName) },

    sourcesInBase := false,

    (unmanagedSourceDirectories in Compile) := Seq(
      baseDirectory.value / "src" / "shared" / "scala",
      baseDirectory.value / "src" / "js" / "scala"
    )
  )

}
