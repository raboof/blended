import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

//#include blended.build/build-versions.scala
//#include blended.build/build-dependencies.scala
//#include blended.build/build-plugins.scala
//#include blended.build/build-common.scala

BlendedModel(
  gav = Blended.blended("blended.reactor"),
  packaging = "pom",
  description = "A collection of bundles to develop OSGi application on top of Scala and Akka and Camel.",
  plugins = Seq(
    Plugin(
      Plugins.nexusStaging,
      extensions = true,
      configuration = Config(
        serverId = "ossrh",
        nexusUrl = "https://oss.sonatype.org/",
        autoReleaseAfterClose = "true",
        skipNexusStagingDeployMojo = "true"
      )
    ),
    skipInstallPlugin,
    skipDeployPlugin
  ),
  profiles = Seq(
    Profile(
      id = "build",
      activation = Activation(
        activeByDefault = true
      ),
      modules = Seq(
        "blended.launcher",
        "blended.updater",
        "blended.updater.config",
        "blended.updater.tools",
        "blended.updater.remote",
        "blended-updater-maven-plugin",
        "blended.activemq.brokerstarter",
        "blended.container.context.api",
        "blended.container.context.impl",
        "blended.util",
        "blended.jmx",
        "blended.camel.utils",
        "blended.jetty.boot",
        "blended.jms.sampler",
        "blended.jms.utils",
        "blended.testsupport",
        "blended.testsupport.pojosr",
        "blended.domino",
        "blended.akka",
        "blended.akka.http.api",
        "blended.akka.http",
        //"blended.mgmt.app",
        "blended.akka.http.jmsqueue",
        "blended.akka.http.proxy",
        "blended.akka.http.restjms",
        "blended.mgmt.base",
        "blended.mgmt.repo",
        "blended.mgmt.repo.rest",
        "blended.mgmt.agent",
        "blended.mgmt.rest",
        "blended.mgmt.mock",
        "blended.mgmt.service.jmx",
        "blended.mgmt.ws",
        "blended.prickle",
        "blended.prickle.akka.http",
        "blended.security",
        "blended.security.scep",
        "blended.security.ssl",
        "blended.security.boot",
        "blended.security.login",
        "blended.security.login.rest",
        "blended.security.akka.http",
        "blended.hawtio.login",
        "blended.persistence",
        "blended.persistence.h2",
        "blended.persistence.orient",
        "blended.jolokia",
        "blended.itestsupport",
        "blended.samples",
        "blended.activemq.defaultbroker",
        "blended.activemq.client",
        "blended.launcher.features",
        "blended.file",
        "blended.demo"
      )
    ),
    Profile(
      id = "itest",
      modules = Seq(
        "blended.itestsupport",
        "blended.itest"
      )
    ),
    Profile(
      id = "docker",
      modules = Seq(
        "blended.docker"
      )
    )
  )
)
