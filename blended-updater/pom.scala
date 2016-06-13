import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

val p = "blended.updater"
println(s"Sourcing ${p}")

#include blended-build/build-common.scala
#include blended-build/build-dependencies.scala
#include blended-build/build-plugins.scala

Model(
  blendedUpdater,
  packaging = "bundle",
  name = "${project.artifactId}",
  description = "OSGi Updater",
  parent = blendedParent,
  dependencies = Seq(
    orgOsgi,
    domino,
    akkaOsgi,
    slf4j,
    typesafeConfig,
    blendedUpdaterConfig,
    blendedLauncher,
    blendedMgmtBase,
    blendedContainerContext,
    blendedAkka,
    blendedSprayApi,
    akkaTestkit % "test",
    scalaTest % "test",
    felixFramework % "test",
    logbackClassic % "test",
    akkaSlf4j % "test",
    felixGogoRuntime % "test",
    felixGogoShell % "test",
    felixGogoCommand % "test",
    felixFileinstall % "test",
    mockitoAll % "test",
    blendedTestSupport % "test"
  ),
  properties = Map(
    "bundle.symbolicName" -> "${project.artifactId}",
    "bundle.namespace" -> "${project.artifactId}"
  ),
  build = Build(
    plugins = Seq(
      mavenBundlePlugin,
      scalaMavenPlugin,
      scalatestMavenPlugin
    )
  ),
  modelVersion = "4.0.0"
)