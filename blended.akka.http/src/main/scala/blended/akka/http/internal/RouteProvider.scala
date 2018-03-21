package blended.akka.http.internal

import domino.capsule.Capsule
import domino.service_watching.ServiceWatching
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import blended.akka.http.HttpContext
import domino.capsule.CapsuleContext
import org.osgi.framework.BundleContext
import domino.service_watching.ServiceWatcherEvent
import domino.service_watching.ServiceWatcherContext
import domino.service_consuming.ServiceConsuming
import blended.akka.http.SimpleHttpContext

class RouteProvider {

  private[this] val log = org.log4s.getLogger

  val initialRoute: Route = path("about") {
    get {
      complete("Blended Akka Http Server")
    }
  }

  @volatile
  private[this] var currentRoute: Route = initialRoute
  private[this] var contexts: Seq[HttpContext] = Seq()

  // We use the fact that route is just a function, so we can change dynamically
  private[this] val fixedDynamicRoute: Route = ctx => currentRoute(ctx)
  // We want a def in the API, but use a val internally
  def dynamicRoute: Route = fixedDynamicRoute

  private[this] def updateRoutes(): Unit = {
    log.debug("Current http contexts prefixes: " + contexts.map(_.prefix).mkString(", "))
    currentRoute = contexts.foldLeft(initialRoute) { (route, context) =>
      route ~ pathPrefix(context.prefix) {
        context.route
      }
    }
  }

  def dynamicAdapt(capsuleContext: CapsuleContext, bundleContext: BundleContext): Unit = {

    def addContext(httpContext: HttpContext): Unit = {
      // we currently allow only one route for each prefix
      contexts = contexts.filter(c => c.prefix != httpContext.prefix)
      contexts :+= httpContext
      updateRoutes()
    }

    def modifyContext(httpContext: HttpContext): Unit = addContext _

    def removeContext(httpContext: HttpContext): Unit = {
      // we currently allow only one route for each prefix
      contexts = contexts.filter(c => c.prefix != httpContext.prefix)
      updateRoutes()
    }

    class WatchCapsule(
      override protected val capsuleContext: CapsuleContext,
      override protected val bundleContext: BundleContext)
        extends Capsule
        with ServiceWatching
        with ServiceConsuming {

      override def start(): Unit = {
        // We listen for supported services and add them to the route
        // wait for context registrations, and add them to the main route
        watchServices[HttpContext] {
          case ServiceWatcherEvent.AddingService(httpContext, watchContext) => addContext(httpContext)
          case ServiceWatcherEvent.ModifiedService(httpContext, watchContext) => modifyContext(httpContext)
          case ServiceWatcherEvent.RemovedService(httpContext, watchContext) => removeContext(httpContext)
        }

        def toContext[S <: AnyRef](route: Route, ctx: ServiceWatcherContext[S]): Option[HttpContext] = {
          Option(ctx.ref.getProperty("context")).collect {
            case prefix: String if !prefix.trim().isEmpty() => SimpleHttpContext(prefix, route)
          }
        }

        watchServices[Route] {
          case ServiceWatcherEvent.AddingService(route, watchContext) => toContext(route, watchContext).foreach(addContext)
          case ServiceWatcherEvent.ModifiedService(route, watchContext) => toContext(route, watchContext).foreach(modifyContext)
          case ServiceWatcherEvent.RemovedService(route, watchContext) => toContext(route, watchContext).foreach(removeContext)
        }

        // Use all contexts that were registered before we came, too
        contexts ++= services[HttpContext]
        updateRoutes()

      }
      override def stop(): Unit = {
        // We unregister all routes
        contexts = Seq()
        updateRoutes()
      }
    }

    val capsule = new WatchCapsule(capsuleContext, bundleContext)
    capsuleContext.addCapsule(capsule)
  }

}