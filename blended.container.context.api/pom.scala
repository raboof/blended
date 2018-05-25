import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

//#include ../blended.build/build-versions.scala
//#include ../blended.build/build-dependencies.scala
//#include ../blended.build/build-plugins.scala
//#include ../blended.build/build-common.scala

BlendedModel(
  gav = Blended.containerContextApi,
  packaging = "bundle",
  description = "The API for the Container Context and Identifier Services",
  dependencies = Seq(
    scalaLib % "provided",
    typesafeConfig,
    log4s,
    scalaTest % "test",
    logbackCore % "test",
    logbackClassic % "test"
  ),
  plugins = Seq(
    mavenBundlePlugin,
    scalaCompilerPlugin,
    scalatestMavenPlugin
  )
)
