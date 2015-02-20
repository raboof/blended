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

package de.woq.blended.itestsupport.docker

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import akka.util.Timeout
import akka.pattern.ask
import com.github.dockerjava.api.model.Ports.Binding
import com.github.dockerjava.api.model.{ExposedPort, Ports}
import scala.concurrent.duration._

import de.woq.blended.itestsupport.docker.protocol._
import de.woq.blended.itestsupport.protocol._

import scala.concurrent.Future

object ContainerActor {
  def apply(container: DockerContainer, portScanner: ActorRef) = new ContainerActor(container, portScanner)
}

class ContainerActor(container: DockerContainer, portScanner: ActorRef) extends Actor with ActorLogging {

  case class PerformStart(container: DockerContainer, ports: Ports)

  object ContainerStartActor {
    def apply() = new ContainerStartActor
  }

  class ContainerStartActor extends Actor with ActorLogging {

    def receive = LoggingReceive {
      case PerformStart(container, ports) =>
        container.startContainer(ports)
        sender ! ContainerStarted(container.containerName)
    }
  }

  implicit val timeout = new Timeout(5.seconds)
  implicit val eCtxt   = context.dispatcher

  var pendingCommands : List[(ActorRef, Any)] = List.empty

  def stopped : Receive = {
    case StartContainer(n) if container.containerName == n  => {
      portBindings(sender)
    }
    case (p : Ports, requestor: ActorRef) => {
      val starter = context.actorOf(Props(ContainerStartActor()))
      context become LoggingReceive(starting(requestor) orElse getPorts )
      starter ! PerformStart(container, p)
    }
    case cmd => pendingCommands ::= (sender, cmd)
  }

  def starting(requestor : ActorRef) : Receive = {
    case msg : ContainerStarted =>
      requestor ! msg
      pendingCommands.reverse.map {
        case (requestor: ActorRef, cmd: Any) => self.tell(cmd, requestor)
      }
      context become LoggingReceive(started orElse getPorts)
    case cmd => pendingCommands ::= (sender, cmd)
  }

  def started : Receive = {
    case StopContainer(n) if container.containerName == n  => {
      val requestor = sender
      container.stopContainer
      context become stopped
      requestor ! ContainerStopped(container.containerName)
    }
    case InspectContainer(n) if container.containerName == n => {
      val requestor = sender
      requestor ! container.containerInfo
    }
  }

  def getPorts : Receive = {
    case GetContainerPorts(n) if container.containerName == n => {
      val ports : Map[String, NamedContainerPort] =
        container.ports.mapValues { namedPort =>
          val exposedPort = new ExposedPort(namedPort.sourcePort)
          val realPort = exposedPort.getPort
          NamedContainerPort(namedPort.name, realPort)
        }
      log.debug(s"Sending [${ContainerPorts(ports)}] to [$sender]")
      sender ! ContainerPorts(ports)
    }
  }

  def receive = LoggingReceive(stopped)

  private def portBindings(requestor: ActorRef) : Unit = {
    val bindings = new Ports()

    // We create a Future for each port. The Future uses the underlying PortScanner
    // to retreive the next port number
    val portRequests : Iterable[Future[(NamedContainerPort, FreePort)]] =
      container.ports.values.map { case namedPort =>
        (portScanner ? GetPort).mapTo[FreePort].collect { case fp =>
          (namedPort, fp)
        }
    }

    // We create a single Future from the list of futures created before, collect the result
    // and then pass on the Bindings to ourselves.
    Future.sequence(portRequests).mapTo[Iterable[(NamedContainerPort, FreePort)]].collect { case ports =>
      ports.foreach { case (namedPort, freeport) =>
        bindings.bind(new ExposedPort(namedPort.sourcePort), new Binding(freeport.p))
      }
    } onSuccess { case _ => self ! (bindings, requestor) }
  }
}