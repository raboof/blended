import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

//#include ../blended.build/build-versions.scala
//#include ../blended.build/build-dependencies.scala
//#include ../blended.build/build-plugins.scala
//#include ../blended.build/build-common.scala

BlendedModel(
  gav = Blended.jmx,
  packaging = "bundle",
  description = "Helper bundle to expose the platform's MBeanServer as OSGI Service.",
  dependencies = Seq(
    Deps.domino
  ),
  plugins = Seq(
    mavenBundlePlugin,
    scalaCompilerPlugin
  )
)
