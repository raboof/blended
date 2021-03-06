import sbt._
import sbt.Keys._

object Dependencies {

  val activeMqVersion = "5.15.3"
  val akkaVersion = "2.5.9"
  val akkaHttpVersion = "10.1.1"
  val camelVersion = "2.17.3"
  val jettyVersion = "9.4.8.v20171121"
  val jolokiaVersion = "1.5.0"
  val microJsonVersion = "1.4"
  val parboiledVersion = "1.1.6"
  val prickleVersion = "1.1.14"
  val scalatestVersion = "3.0.5"
  val slf4jVersion = "1.7.25"
  val sprayVersion = "1.3.4"
  val springVersion = "3.2.18.RELEASE_1"

  private[this] def akka(m: String) = "com.typesafe.akka" %% s"akka-${m}" % akkaVersion
  private[this] def akka_Http(m: String) = "com.typesafe.akka" %% s"akka-${m}" % akkaHttpVersion

  val activeMqBroker = "org.apache.activemq" % "activemq-broker" % activeMqVersion
  val activeMqClient = "org.apache.activemq" % "activemq-client" % activeMqVersion
  val activeMqKahadbStore = "org.apache.activemq" % "activemq-kahadb-store" % activeMqVersion
  val activeMqSpring = "org.apache.activemq" % "activemq-spring" % activeMqVersion
  val akkaActor = akka("actor")
  val akkaCamel = akka("camel")
  val akkaHttp = akka_Http("http")
  val akkaHttpCore = akka_Http("http-core")
  val akkaHttpTestkit = akka_Http("http-testkit")
  val akkaOsgi = akka("osgi")
  val akkaParsing = akka_Http("parsing")
  val akkaStream = akka("stream")
  val akkaTestkit = akka("testkit")
  val akkaSlf4j = akka("slf4j")

  val bouncyCastleBcprov = "org.bouncycastle" % "bcprov-jdk15on" % "1.60"
  val bouncyCastlePkix = "org.bouncycastle" % "bcpkix-jdk15on" % "1.60"

  val camelCore = "org.apache.camel" % "camel-core" % camelVersion

  val camelJms = "org.apache.camel" % "camel-jms" % camelVersion
  val cmdOption = "de.tototec" % "de.tototec.cmdoption" % "0.6.0"
  val commonsBeanUtils = "commons-beanutils" % "commons-beanutils" % "1.9.3"
  val commonsCodec = "commons-codec" % "commons-codec" % "1.11"
  val commonsDaemon = "commons-daemon" % "commons-daemon" % "1.0.15"
  val commonsIo = "commons-io" % "commons-io" % "2.6"
  val commonsLang2 = "commons-lang" % "commons-lang" % "2.6"
  val concurrentLinkedHashMapLru = "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.2"

  val domino = "com.github.domino-osgi" %% "domino" % "1.1.3-SNAPSHOT"

  val felixConnect = "org.apache.felix" % "org.apache.felix.connect" % "0.1.0"
  val felixGogoCommand = "org.apache.felix" % "org.apache.felix.gogo.command" % "0.14.0"
  val felixGogoRuntime = "org.apache.felix" % "org.apache.felix.gogo.runtime" % "0.16.2"
  val felixGogoShell = "org.apache.felix" % "org.apache.felix.gogo.shell" % "0.10.0"
  val felixFileinstall = "org.apache.felix" % "org.apache.felix.fileinstall" % "3.4.2"
  val felixFramework = "org.apache.felix" % "org.apache.felix.framework" % "5.6.10"

  val geronimoJms11Spec = "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.1.1"

  val h2 = "com.h2database" % "h2" % "1.4.197"
  val hikaricp = "com.zaxxer" % "HikariCP" % "3.1.0"

  val jcip = "net.jcip" % "jcip-annotations" % "1.0"
  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  private def jettyOsgi(n: String) = "org.eclipse.jetty.osgi" % s"jetty-$n" % jettyVersion
  val jettyOsgiBoot = jettyOsgi("osgi-boot")
  val jjwt = "io.jsonwebtoken" % "jjwt" % "0.7.0"
  val jms11Spec = "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.1.1"
  val jolokiaJvm = "org.jolokia" % "jolokia-jvm" % jolokiaVersion
  val jolokiaJvmAgent = jolokiaJvm.classifier("agent")
  val jscep = "com.google.code.jscep" % "jscep" % "2.5.0"
  val jsonLenses = "net.virtual-void" %% "json-lenses" % "0.6.2"
  val julToSlf4j = "org.slf4j" % "jul-to-slf4j" % slf4jVersion
  val junit = "junit" % "junit" % "4.11"

  val lambdaTest = "de.tototec" % "de.tobiasroeser.lambdatest" % "0.6.2"
  val liquibase = "org.liquibase" % "liquibase-core" % "3.6.1"
  val log4s = "org.log4s" %% "log4s" % "1.6.1"
  val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.3"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val microjson = "com.github.benhutchison" %% "microjson" % microJsonVersion
  val mimepull = "org.jvnet.mimepull" % "mimepull" % "1.9.5"
  val mockitoAll = "org.mockito" % "mockito-all" % "1.9.5"

  val orgOsgi = "org.osgi" % "org.osgi.core" % "6.0.0"
  val orgOsgiCompendium = "org.osgi" % "org.osgi.compendium" % "5.0.0"

  val parboiledCore = "org.parboiled" % "parboiled-core" % parboiledVersion
  val parboiledScala = "org.parboiled" %% "parboiled-scala" % parboiledVersion
  val prickle = "com.github.benhutchison" %% "prickle" % prickleVersion

  // SCALA
  val scalaLibrary = Def.setting("org.scala-lang" % "scala-library" % scalaVersion.value)
  val scalaReflect = Def.setting("org.scala-lang" % "scala-reflect" % scalaVersion.value)
  val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.1.0"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val shapeless = "com.chuusai" %% "shapeless" % "1.2.4"
  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  val slf4jLog4j12 = "org.slf4j" % "slf4j-log4j12" % slf4jVersion
  val snakeyaml = "org.yaml" % "snakeyaml" % "1.18"
  val sprayJson = "io.spray" %% s"spray-json" % sprayVersion
  private def spring(n: String) = "org.apache.servicemix.bundles" % s"org.apache.servicemix.bundles.spring-${n}" % springVersion
  val springBeans = spring("beans")
  //val springAop = spring("aop")
  //val springContext = spring("context")
  //val springContextSupport = spring("context-support")
  //val springExpression = spring("expression")
  val springCore = spring("core")
  val springJdbc = spring("jdbc")
  // val springJms = spring("jms")
  val springTx = spring("tx")
  val sttp = "com.softwaremill.sttp" %% "core" % "1.3.0"
  val sttpAkka = "com.softwaremill.sttp" %% "akka-http-backend" % "1.3.0"

  val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
  val typesafeConfigSSL = "com.typesafe" %% "ssl-config-core" % "0.2.4"

  val wiremock = "com.github.tomakehurst" % "wiremock" % "2.1.11"
  val wiremockStandalone = "com.github.tomakehurst" % "wiremock-standalone" % "2.1.11"

}

