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

package de.wayofquality.blended.persistence.protocol

import akka.actor.{ActorRef, Terminated}
import akka.event.LoggingReceive
import de.wayofquality.blended.akka.protocol._
import de.wayofquality.blended.akka.{MemoryStash, OSGIActor}
import de.wayofquality.blended.persistence.internal.PersistenceBundleName
import org.osgi.framework.BundleContext

trait DataObjectFactory {
  def createObject(props: PersistenceProperties) : Option[DataObject]
}

class DataObjectCreator(factory: DataObjectFactory, bc: BundleContext) extends OSGIActor with PersistenceBundleName with MemoryStash {

  def createObject(props: PersistenceProperties) : Option[DataObject] = factory.createObject(props)

  override protected def bundleContext: BundleContext = bc

  override def preStart(): Unit = {
    super.preStart()
    context.system.eventStream.subscribe(self, classOf[BundleActorStarted])
  }

  override def receive = registering orElse stashing

  def registering : Receive = LoggingReceive {
    case BundleActorStarted(name) if name == bundleSymbolicName =>
      setupFactory()
      unstash()
      context.become(working)
  }

  def working : Receive = LoggingReceive {
    case CreateObjectFromProperties(props) =>
      createObject(props) match {
        case Some(dataObject) =>
          log.debug(s"Created data object [${dataObject.toString}].")
          sender ! QueryResult(List(dataObject))
        case _ =>
      }
    case Terminated(actor) => context.become(registering)
  }

  def setupFactory() : Unit = {
    context.system.eventStream.subscribe(self, classOf[BundleActorStarted])

    (for(actor <- bundleActor(bundleSymbolicName).mapTo[ActorRef]) yield actor) map  {
      _ match {
        case actor : ActorRef =>
          log.debug("Registering data factory with persistence manager")
          actor ! RegisterDataFactory(self)
          context.watch(actor)
        case dlq if dlq == context.system.deadLetters =>
      }
    }
  }
}
