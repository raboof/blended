/*
 * Copyright 2014ff, WoQ - Way of Quality UG(mbH)
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

package de.woq.osgi.akka.system.internal

import akka.actor._
import org.osgi.framework.BundleContext
import de.woq.osgi.akka.modules._
import akka.util.Timeout
import scala.concurrent.duration._
import de.woq.osgi.akka.system.WOQAkkaConstants._
import de.woq.osgi.java.container.context.ContainerContext
import akka.event.LoggingReceive
import de.woq.osgi.akka.system.OSGIProtocol.GetService
import de.woq.osgi.akka.system.{OSGIProtocol, ConfigLocatorRequest}
import scala.Some
import akka.actor.Props

object OSGIFacade {

  def apply()(implicit bundleContext : BundleContext) = new OSGIFacade()
}

class OSGIFacade(implicit bundleContext : BundleContext) extends Actor with ActorLogging {

  implicit val timeout = Timeout(1.second)
  implicit val ec = context.dispatcher

  var configLocator : ActorRef = context.system.deadLetters
  var references : ActorRef = context.system.deadLetters

  override def preStart() {

    log info "Creating Config Locator actor"
    configLocator = context.actorOf(Props(ConfigLocator(configDir)), configLocatorPath)

    log info "Creating OSGI References handler"
    references = context.actorOf(Props(OSGIReferences()(bundleContext)), referencesPath)
  }

  override def receive = LoggingReceive {
    case GetService(clazz) => references forward OSGIProtocol.CreateReference(clazz)
    case cfgRequest : ConfigLocatorRequest => configLocator forward(cfgRequest)
  }

  private[OSGIFacade] def configDir = {

    val defaultConfigDir = System.getProperty("karaf.home") + "/etc"

    (bundleContext findService(classOf[ContainerContext])) match {
      case Some(svcRef) => svcRef invokeService { ctx => ctx.getContainerConfigDirectory } match {
        case Some(s)  => s
        case _ => defaultConfigDir
      }
      case _ => defaultConfigDir
    }
  }
}