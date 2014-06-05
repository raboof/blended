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

package de.woq.osgi.java.container.registry.internal

import akka.actor.{ActorLogging, Actor}
import de.woq.osgi.akka.system.{BundleName, OSGIActor}
import org.osgi.framework.BundleContext
import de.woq.osgi.java.container.registry.RegistryBundleName
import de.woq.osgi.java.container.registry.protocol._

object ContainerRegistryImpl {
  def apply()(implicit bundleContext: BundleContext) = new ContainerRegistryImpl() with OSGIActor with RegistryBundleName
}

class ContainerRegistryImpl(implicit bundleContext : BundleContext) extends Actor with ActorLogging { this : OSGIActor with BundleName =>

  def receive = {
    case UpdateContainerInfo(info) => {
      log info(s"${info.toString}")
      sender ! ContainerRegistryResponseOK(info.containerId)
    }
  }
}