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

package de.wayofquality.blended.akka

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import de.wayofquality.blended.akka.protocol._
import org.helgoboss.domino.DominoActivator

trait BundleName {
  def bundleSymbolicName : String
}

trait ActorSystemAware extends DominoActivator { this : BundleName =>

  def startBundleActor(system: ActorSystem, name: String) : ActorRef

  whenBundleActive {
    whenServicePresent[ActorSystem] { system =>
      log debug s"Preparing bundle actor for [$bundleSymbolicName]."

      val actorRef = startBundleActor(system, bundleSymbolicName)
      actorRef ! InitializeBundle(bundleContext)

      system.eventStream.publish(BundleActorStarted(bundleSymbolicName))
      postStartBundleActor()

      onStop {
        preStopBundleActor()
        actorRef ! PoisonPill
      }
    }
  }

  def postStartBundleActor() : Unit = {}

  def preStopBundleActor() : Unit = {}
}
