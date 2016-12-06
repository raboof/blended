implicit val scalaVersion = ScalaVersion(BlendedVersions.scalaVersion)
val scalaJsBinVersion = ScalaVersion(BlendedVersions.scalaJsVersion).binaryVersion

// Dependencies
val activationApi = "org.apache.servicemix.specs" % "org.apache.servicemix.specs.activation-api-1.1" % "2.2.0"

val activeMqBroker = "org.apache.activemq" % "activemq-broker" % BlendedVersions.activeMqVersion
val activeMqClient = "org.apache.activemq" % "activemq-client" % BlendedVersions.activeMqVersion
val activeMqSpring = "org.apache.activemq" % "activemq-spring" % BlendedVersions.activeMqVersion
val activeMqOsgi = "org.apache.activemq" % "activemq-osgi" % BlendedVersions.activeMqVersion
val activeMqKahadbStore = "org.apache.activemq" % "activemq-kahadb-store" % BlendedVersions.activeMqVersion
    
val akkaActor = "com.typesafe.akka" %% "akka-actor" % BlendedVersions.akkaVersion
val akkaCamel = "com.typesafe.akka" %% "akka-camel" % BlendedVersions.akkaVersion
val akkaOsgi = "com.typesafe.akka" %% "akka-osgi" % BlendedVersions.akkaVersion
val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % BlendedVersions.akkaVersion
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % BlendedVersions.akkaVersion

val aopAlliance = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.aopalliance" % "1.0_6"

val apacheShiroCore = "org.apache.shiro" % "shiro-core" % BlendedVersions.apacheShiroVersion
val apacheShiroWeb = "org.apache.shiro" % "shiro-web" % BlendedVersions.apacheShiroVersion

val ariesBlueprintApi = "org.apache.aries.blueprint" % "org.apache.aries.blueprint.api" % "1.0.1"
val ariesBlueprintCore = "org.apache.aries.blueprint" % "org.apache.aries.blueprint.core" % "1.4.3"
val ariesJmxApi = "org.apache.aries.jmx" % "org.apache.aries.jmx.api" % "1.1.1"
val ariesJmxCore = "org.apache.aries.jmx" % "org.apache.aries.jmx.core" % "1.1.1"
val ariesProxyApi = "org.apache.aries.proxy" % "org.apache.aries.proxy.api" % "1.0.1"
val ariesUtil = "org.apache.aries" % "org.apache.aries.util" % "1.1.0"

val asmAll = "org.ow2.asm" % "asm-all" % "4.1"
val bndLib = "biz.aQute.bnd" % "biz.aQute.bndlib" % "3.2.0"

val camelCore = "org.apache.camel" % "camel-core" % BlendedVersions.camelVersion
val camelJms = "org.apache.camel" % "camel-jms" % BlendedVersions.camelVersion
val camelHttp = "org.apache.camel" % "camel-http" % BlendedVersions.camelVersion
val camelHttpCommon = "org.apache.camel" % "camel-http-common" % BlendedVersions.camelVersion
val camelServlet = "org.apache.camel" % "camel-servlet" % BlendedVersions.camelVersion
val camelServletListener = "org.apache.camel" % "camel-servletlistener" % BlendedVersions.camelVersion
val camelSpring = "org.apache.camel" % "camel-spring" % BlendedVersions.camelVersion

val commonsBeanUtils = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.commons-beanutils" % "1.8.3_2"
val commonsCodec = "org.apache.commons" % "com.springsource.org.apache.commons.codec" % "1.6.0"
val commonsCollections = "org.apache.commons" % "com.springsource.org.apache.commons.collections" % "3.2.1"
val commonsDaemon = "commons-daemon" % "commons-daemon" % "1.0.15"
val commonsDiscovery = "org.apache.commons" % "com.springsource.org.apache.commons.discovery" % "0.4.0"
val commonsExec = "org.apache.commons" % "commons-exec" % "1.3"
val commonsHttpclient = "org.apache.commons" % "com.springsource.org.apache.commons.httpclient" % "3.1.0"
val commonsIo = "org.apache.commons" % "com.springsource.org.apache.commons.io" % "1.4.0"
val commonsLang = "commons-lang" % "commons-lang" % "2.6"
val commonsNet = "commons-net" % "commons-net" % "3.3"
val commonsPool = "commons-pool" % "commons-pool" % "1.6"

val cmdOption = "de.tototec" % "de.tototec.cmdoption" % "0.4.2"
val concurrentLinkedHashMapLru = "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.2"
    
val domino = "com.github.domino-osgi" %% "domino" % "1.1.1"

val felixConfigAdmin = "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.6"
val felixEventAdmin = "org.apache.felix" % "org.apache.felix.eventadmin" % "1.3.2"
val felixFramework = "org.apache.felix" % "org.apache.felix.framework" % "5.0.0"
val felixFileinstall = "org.apache.felix" % "org.apache.felix.fileinstall" % "3.4.2"
val felixGogoCommand = "org.apache.felix" % "org.apache.felix.gogo.command" % "0.14.0"
val felixGogoShell = "org.apache.felix" % "org.apache.felix.gogo.shell" % "0.10.0"
val felixGogoRuntime = "org.apache.felix" % "org.apache.felix.gogo.runtime" % "0.16.2"
val felixMetatype = "org.apache.felix" % "org.apache.felix.metatype" % "1.0.12"

val geronimoAnnotation = "org.apache.geronimo.specs" % "geronimo-annotation_1.1_spec" % "1.0.1"
val geronimoJaspic = "org.apache.geronimo.specs" % "geronimo-jaspic_1.0_spec" % "1.1"
val geronimoJ2eeMgmtSpec = "org.apache.geronimo.specs" % "geronimo-j2ee-management_1.1_spec" % "1.0.1"
val geronimoJms11Spec = "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.1.1"
val geronimoServlet25Spec = "org.apache.geronimo.specs" % "geronimo-servlet_2.5_spec" % "1.2"
val geronimoServlet30Spec = "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0"

val hawtioWeb = Dependency(gav = "io.hawt" % "hawtio-web" % "1.4.65", `type` = "war")

val javaxMail = "javax.mail" % "mail" % "1.4.5"
val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % BlendedVersions.slf4jVersion

val jacksonCoreAsl = "org.codehaus.jackson" % "jackson-core-asl" % "1.9.12"
val jacksonMapperAsl = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.12"
val jacksonJaxrs = "org.codehaus.jackson" % "jackson-jaxrs" % "1.9.12"
val jettison = "org.codehaus.jettison" % "jettison" % "1.3.4"

val jerseyClient = "com.sun.jersey" % "jersey-client" % "1.18.1"
val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.18.1"
val jerseyJson = "com.sun.jersey" % "jersey-json" % "1.18.1"
val jerseyServer = "com.sun.jersey" % "jersey-server" % "1.18.1"
val jerseyServlet = "com.sun.jersey" % "jersey-servlet" % "1.18.1"

val jettyServer = "org.eclipse.jetty.aggregate" % "jetty-all-server" % "8.1.19.v20160209"
val jms11Spec = "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "1.1.1"
val jsonLenses = "net.virtual-void" %% "json-lenses" % "0.5.4"
val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.1"
val junit = "junit" % "junit" % "4.11"
val julToSlf4j = "org.slf4j" % "jul-to-slf4j" % BlendedVersions.slf4jVersion

val lambdaTest = "de.tototec" % "de.tobiasroeser.lambdatest" % "0.2.4"
val logbackCore = "ch.qos.logback" % "logback-core" % "1.1.3"
val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"

val mockitoAll = "org.mockito" % "mockito-all" % "1.9.5"

val paxSwissboxCore = "org.ops4j.pax.swissbox" % "pax-swissbox-core" % "1.7.0"
val paxSwissboxOptJcl = "org.ops4j.pax.swissbox" % "pax-swissbox-optional-jcl" % "1.7.0"

val ops4jBaseLang = "org.ops4j.base" % "ops4j-base-lang" % "1.4.0"

val orientDbCore = "com.orientechnologies" % "orientdb-core" % "2.2.7"
val orgOsgi = "org.osgi" % "org.osgi.core" % "5.0.0"
val orgOsgiCompendium = "org.osgi" % "org.osgi.compendium" % "5.0.0"

val paxwebApi = "org.ops4j.pax.web" % "pax-web-api" % "3.1.0"
val paxwebExtWhiteboard = "org.ops4j.pax.web" % "pax-web-extender-whiteboard" % "3.1.0"
val paxwebExtWar = "org.ops4j.pax.web" % "pax-web-extender-war" % "3.1.0"
val paxwebJetty = "org.ops4j.pax.web" % "pax-web-jetty" % "3.1.0"
val paxwebJsp = "org.ops4j.pax.web" % "pax-web-jsp" % "3.1.0"
val paxwebRuntime = "org.ops4j.pax.web" % "pax-web-runtime" % "3.1.0"
val paxwebSpi = "org.ops4j.pax.web" % "pax-web-spi" % "3.1.0"

val scalaLib = "org.scala-lang" % "scala-library" % BlendedVersions.scalaVersion
val scalaReflect = "org.scala-lang" % "scala-reflect" % BlendedVersions.scalaVersion
val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4"
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"

val servicemixJaxbApi = "org.apache.servicemix.specs" % "org.apache.servicemix.specs.jaxb-api-2.2" % "2.5.0"
val servicemixJaxbImpl = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.jaxb-impl" % "2.2.1.1_2"
val servicemixJaxbRuntime = "org.jvnet.jaxb2_commons" % "jaxb2-basics-runtime" % "0.6.4"
val servicemixStaxApi = "org.apache.servicemix.specs" % "org.apache.servicemix.specs.stax-api-1.0" % "2.4.0"

val shiroCore = "org.apache.shiro" % "shiro-core" % BlendedVersions.apacheShiroVersion
val shiroWeb = "org.apache.shiro" % "shiro-web" % BlendedVersions.apacheShiroVersion

val slf4j = "org.slf4j" % "slf4j-api" % BlendedVersions.slf4jVersion
val slf4jJcl = "org.slf4j" % "jcl-over-slf4j" % BlendedVersions.slf4jVersion
val slf4jJul = "org.slf4j" % "jul-to-slf4j" % BlendedVersions.slf4jVersion
val slf4jLog4j12 = "org.slf4j" % "slf4j-log4j12" % BlendedVersions.slf4jVersion

val sprayClient = "io.spray" %% "spray-client" % BlendedVersions.sprayVersion
val sprayCaching = "io.spray" %% "spray-caching" % BlendedVersions.sprayVersion
val sprayHttp = "io.spray" %% "spray-http" % BlendedVersions.sprayVersion
val sprayHttpx = "io.spray" %% "spray-httpx" % BlendedVersions.sprayVersion
val sprayIo = "io.spray" %% "spray-io" % BlendedVersions.sprayVersion
val sprayJson = "io.spray" %% "spray-json" % BlendedVersions.sprayVersion
val sprayRouting = "io.spray" %% "spray-routing" % BlendedVersions.sprayVersion
val sprayServlet = "io.spray" %% "spray-servlet" % BlendedVersions.sprayVersion
val sprayTestkit = "io.spray" %% "spray-testkit" % BlendedVersions.sprayVersion
val sprayUtil = "io.spray" %% "spray-util" % BlendedVersions.sprayVersion

val springBeans = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-beans" % "3.2.14.RELEASE_1"
val springAop = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-aop" % "3.2.14.RELEASE_1"
val springContext = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-context" % "3.2.14.RELEASE_1"
val springContextSupport = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-context-support" % "3.2.14.RELEASE_1"
val springExpression = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-expression" % "3.2.14.RELEASE_1"
val springCore = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-core" % "3.2.14.RELEASE_1"
val springJms = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-jms" % "3.2.14.RELEASE_1"
val springTx = "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.spring-tx" % "3.2.14.RELEASE_1"

val shapeless = "com.chuusai" %% "shapeless" % "1.2.4"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

val upickle = "com.lihaoyi" %% "upickle" % BlendedVersions.upickle

val wiremock = "com.github.tomakehurst" % "wiremock" % "2.1.11"
val wiremockStandalone = "com.github.tomakehurst" % "wiremock-standalone" % "2.1.11"

val xbeanAsmShaded = "org.apache.xbean" % "xbean-asm4-shaded" % "3.16"
val xbeanBundleUtils = "org.apache.xbean" % "xbean-bundleutils" % "3.16"
val xbeanFinder = "org.apache.xbean" % "xbean-finder-shaded" % "3.16"
val xbeanReflect = "org.apache.xbean" % "xbean-reflect" % "3.16"
val xbeanSpring = "org.apache.xbean" % "xbean-spring" % "3.16"

// Blended Projects

object BlendedModule {
  def apply(name : String) = BlendedVersions.blendedGroupId % name % BlendedVersions.blendedVersion
}

val blendedParent = Parent(
  gav = BlendedModule("blended.parent"),
  relativePath = "../blended.parent"
)

val blendedActivemqBrokerstarter = BlendedModule("blended.activemq.brokerstarter")
val blendedActivemqClient = BlendedModule("blended.activemq.client")
val blendedActivemqDefaultbroker = BlendedModule("blended.activemq.defaultbroker")
val blendedAkka = BlendedModule("blended.akka")
val blendedAkkaItest = BlendedModule("blended.akka.itest")
val blendedCamelUtils = BlendedModule("blended.camel.utils")
val blendedContainerContext = BlendedModule("blended.container.context")
val blendedContainerRegistry = BlendedModule("blended.container.registry")
val blendedDemoReactor = BlendedModule("blended.demo.reactor")
val blendedDemoMgmt = BlendedModule("blended.demo.mgmt")
val blendedDemoNode = BlendedModule("blended.demo.node")
val blendedDockerReactor = BlendedModule("blended.docker.reactor")
val blendedDockerDemoNode = BlendedModule("blended.docker.demo.node")
val blendedDockerDemoMgmt = BlendedModule("blended.docker.demo.mgmt")
val blendedDomino = BlendedModule("blended.domino")
val blendedHawtioLogin = BlendedModule("blended.hawtio.login")
val blendedItestSupport = BlendedModule("blended.itestsupport")
val blendedJmsUtils = BlendedModule("blended.jms.utils")
val blendedJmx = BlendedModule("blended.jmx")
val blendedJolokia = BlendedModule("blended.jolokia")
val blendedLauncher = BlendedModule("blended.launcher")
val blendedLauncherFeatures = BlendedModule("blended.launcher.features")
val blendedMgmtAgent = BlendedModule("blended.mgmt.agent")
val blendedMgmtBase = BlendedModule("blended.mgmt.base")
val blendedMgmtRepo = BlendedModule("blended.mgmt.repo")
val blendedMgmtRepoRest = BlendedModule("blended.mgmt.repo.rest")
val blendedMgmtMock = BlendedModule("blended.mgmt.mock")
val blendedMgmtRest = BlendedModule("blended.mgmt.rest")
val blendedMgmtUi = BlendedModule("blended.mgmt.ui")
val blendedPersistence = BlendedModule("blended.persistence")
val blendedPersistenceOrient = BlendedModule("blended.persistence.orient")
val blendedSamplesReactor = BlendedModule("blended.samples.reactor")
val blendedSamplesCamel = BlendedModule("blended.samples.camel")
val blendedSamplesJms = BlendedModule("blended.samples.jms")
val blendedSamplesSprayHelloworld = BlendedModule("blended.samples.spray.helloworld")
val blendedSecurity = BlendedModule("blended.security")
val blendedSecurityBoot = BlendedModule("blended.security.boot")
val blendedSpray = BlendedModule("blended.spray")
val blendedSprayApi = BlendedModule("blended.spray.api")
val blendedTestSupport = BlendedModule("blended.testsupport")
val blendedUpdater = BlendedModule("blended.updater")
val blendedUpdaterConfig = BlendedModule("blended.updater.config")
val blendedUpdaterMavenPlugin = BlendedModule("blended-updater-maven-plugin")
val blendedUpdaterRemote = BlendedModule("blended.updater.remote")
val blendedUpdaterTools = BlendedModule("blended.updater.tools")
val blendedUtil = BlendedModule("blended.util")

