/*
 * Copyright 2014ff, WoQ - Way of Quality GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wayofquality.blended.samples.spray.helloworld.internal

import akka.actor.{ActorLogging, Actor}
import de.wayofquality.blended.akka.{BundleName, OSGIActor}
import de.wayofquality.blended.spray.{SprayOSGIServlet, SprayOSGIBridge}
import spray.http.MediaTypes._
import org.osgi.framework.BundleContext
import spray.servlet.ConnectorSettings
import akka.event.LoggingReceive
import spray.util.LoggingContext
import spray.http.Uri.Path

import spray.routing._
import akka.pattern._

import de.wayofquality.blended.modules._
import de.wayofquality.blended.akka.protocol._

trait HelloService extends HttpService {

  val helloRoute = path("hello") {
    get {
      respondWithMediaType(`text/html`) {
        complete {
          <html>
            <body>Say hello to <i>spray routing</i> within OSGi.</body>
          </html>
        }
      }
    }
  }
}

object HelloRoute {
  def apply(contextPath: String)(implicit bundleContext: BundleContext) =
    new HelloRoute(contextPath) with OSGIActor with HelloBundleName
}

class HelloRoute(contextPath: String)(implicit bundleContext: BundleContext)
  extends HelloService with Actor with ActorLogging { this : OSGIActor with BundleName =>

  override implicit def actorRefFactory = context

  def receive = initializing

  def initializing = LoggingReceive {
    case InitializeBundle(_) => getActorConfig(bundleSymbolicName) pipeTo (self)
    case ConfigLocatorResponse(id, cfg) if id == bundleSymbolicName => {

      implicit val servletSettings = ConnectorSettings(cfg).copy(rootPath = Path(s"/$contextPath"))
      implicit val routingSettings = RoutingSettings(cfg)
      implicit val routeLogger = LoggingContext.fromAdapter(log)
      implicit val exceptionHandler = ExceptionHandler.default
      implicit val rejectionHandler = RejectionHandler.Default

      val actorSys = context.system
      val routingActor = self

      val servlet = new SprayOSGIServlet with SprayOSGIBridge {
        override def routeActor = routingActor
        override def connectorSettings = servletSettings
        override def actorSystem = actorSys
      }

      bundleContext.createService(
        servlet, Map(
          "urlPatterns" -> "/",
          "Webapp-Context" -> contextPath,
          "Web-ContextPath" -> s"/$contextPath",
          "servlet-name" -> "hello"
        ))

      context.become(LoggingReceive { runRoute(helloRoute) })
    }
  }
}