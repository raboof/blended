import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

//#include ../blended.build/build-versions.scala
//#include ../blended.build/build-dependencies.scala
//#include ../blended.build/build-plugins.scala
//#include ../blended.build/build-common.scala

BlendedModel(
  gav = Blended.samplesReactor,
  packaging = "pom",
  description = "A collection of sample projects.",
  profiles = Seq(
    Profile(
      id = "build",
      activation = Activation(
        activeByDefault = true
      ),
      modules = Seq(
        "blended.samples.camel",
        "blended.samples.jms",
        "blended.akka.http.sample.helloworld"
      )
    )
  )
)
