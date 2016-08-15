import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

#include ../blended-build/build-common.scala
#include ../blended-build/build-dependencies.scala
#include ../blended-build/build-plugins.scala

BlendedModel(
  gav = blendedJmx,
  packaging = "bundle",
  description = "Helper bundle to expose the platform's MBeanServer as OSGI Service.",
  dependencies = Seq(
    domino
  ),
  plugins = Seq(
    mavenBundlePlugin,
    scalaMavenPlugin
  )
)
