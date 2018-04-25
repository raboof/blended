package blended.akka.http.internal

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import blended.akka.ActorSystemWatching
import domino.DominoActivator
import blended.util.config.Implicits._
import javax.net.ssl.SSLContext
import akka.http.scaladsl.ConnectionContext

import scala.util.{Failure, Success}

class BlendedAkkaHttpActivator extends DominoActivator with ActorSystemWatching {

  private[this] val log = org.log4s.getLogger

  whenBundleActive {

    // reuse the blended akka system
    whenActorSystemAvailable { cfg =>

      val config = cfg.config

      val httpHost = config.getString("host", "0.0.0.0")
      val httpPort = config.getInt("port", 8080)

      val httpsHost = config.getString("ssl.host", "0.0.0.0")
      val httpsPort = config.getInt("ssl.port", 8443)

      implicit val actorSysten = cfg.system
      implicit val actorMaterializer = ActorMaterializer()
      // needed for the future flatMap/onComplete in the end
      implicit val executionContext = actorSysten.dispatcher

      val dynamicRoutes = new RouteProvider()

      log.info(s"Starting HTTP server at ${httpHost}:${httpPort}")
      val bindingFuture = Http().bindAndHandle(dynamicRoutes.dynamicRoute, httpHost, httpPort)

      bindingFuture.onComplete {
        case Success(b) =>
          log.info(s"Started HTTP server at ${b.localAddress}")
        case Failure(t) =>
          log.error(t)("Failed to start Akka Http Server")
          throw t
      }

      onStop {
        log.info(s"Stopping HTTP server at ${httpHost}:${httpPort}")
        bindingFuture.map(serverBinding => serverBinding.unbind())
      }

      log.debug("Listening for SSLContext registrations of type=server...")
      whenAdvancedServicePresent[SSLContext]("(type=server)") { sslContext =>
        
        log.info(s"Detected an server SSLContext. Starting HTTPS server at ${httpsHost}:${httpsPort}")
        
        val https = ConnectionContext.https(sslContext)
        val httpsBindingFuture = Http().bindAndHandle(
          handler = dynamicRoutes.dynamicRoute,
          interface = httpsHost,
          port = httpsPort,
          connectionContext = https)
        httpsBindingFuture.foreach { b =>
          log.info(s"Started HTTPS server at ${b.localAddress}")
        }

        onStop {
          log.info(s"Stopping HTTPS server at ${httpsHost}:${httpsPort}")
          httpsBindingFuture.map(serverBinding => serverBinding.unbind())
        }

      }

      // Consume routes from OSGi Service Registry (white-board pattern)
      dynamicRoutes.dynamicAdapt(capsuleContext, bundleContext)

    }
  }

}


