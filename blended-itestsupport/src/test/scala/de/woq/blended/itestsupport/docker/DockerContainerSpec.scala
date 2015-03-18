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

import com.github.dockerjava.api.model.Link
import de.woq.blended.itestsupport.NamedContainerPort
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{DoNotDiscover, Matchers, WordSpec}

class DockerContainerSpec extends WordSpec
  with Matchers
  with DockerTestSetup
  with MockitoSugar {

  "A Docker Container should" should {

    "be created from the image id and a name" in {
      val container = new DockerContainer(imageId, ctName)

      verify(mockClient).createContainerCmd(imageId)
      verify(createCmd).withName(ctName)
    }

    "issue the stop command with the correct id" in {
      val container = new DockerContainer(imageId, ctName)
      container.stopContainer

      verify(mockClient).stopContainerCmd(ctName)
    }

    "issue the start command with the correct id" in {
      val container = new DockerContainer(imageId, ctName)
      container.startContainer

      verify(mockClient).startContainerCmd(ctName)
    }

    "issue the InspectContainerCommand with the correct id" in {
      val container = new DockerContainer(imageId, ctName)
      container.containerInfo

      verify(mockClient).inspectContainerCmd(ctName)
    }

    "allow to set the linked containers" in {
      val container = new DockerContainer(imageId, ctName)
      container
        .withLink("foo_0:foo")
        .withLink("bar_0:bar")

      val links = container.links

      links should contain theSameElementsAs Vector(
        Link.parse("foo_0:foo"),
        Link.parse("bar_0:bar")
      )
    }

    "allow to set single exposed ports" in {
      val container = new DockerContainer(imageId, ctName)
      val namedPort : NamedContainerPort = ("jmx", 1099, 1099)
      container.withNamedPort(namedPort)

      val ports = container.ports should be (Map("jmx" -> namedPort))
    }

    "allow to set multiple exposed ports" in {
      val container = new DockerContainer(imageId, ctName)
      val port1 : NamedContainerPort = ("jmx", 1099, 1099)
      val port2 : NamedContainerPort = ("http", 8181, 8181)
      container.withNamedPort(port1).withNamedPort(port2)

      val ports = container.ports should be (Map("jmx" -> port1, "http" -> port2))
    }

    "allow to set multiple exposed ports at once" in {
      val container = new DockerContainer(imageId, ctName)
      val port1 : NamedContainerPort = ("jmx", 1099, 1099)
      val port2 : NamedContainerPort = ("http", 8181, 8181)
      container.withNamedPorts(Seq(port1, port2))

      val ports = container.ports should be (Map("jmx" -> port1, "http" -> port2))
    }
  }
}
