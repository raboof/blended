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

package de.woq.osgi.akka.system

import akka.actor.Props
import org.scalatest.{WordSpec, BeforeAndAfterAll, Matchers}
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.mock.MockitoSugar
import de.woq.osgi.akka.system.osgi.OSGIProtocol
import de.woq.osgi.java.testsupport.TestActorSys
import de.woq.osgi.akka.system.osgi.internal.OSGIFacade

class OSGIFacadeSpec extends WordSpec
  with Matchers
  with AssertionsForJUnit
  with BeforeAndAfterAll {

  "OSGIFacade" should {

    "handle config requests correctly" in new TestActorSys with TestSetup with MockitoSugar {
      apply {
        val facade = system.actorOf(Props(OSGIFacade(osgiContext)), "facade")
        facade ! ConfigLocatorRequest("foo")
        expectMsgAllClassOf(classOf[ConfigLocatorResponse]) foreach { m =>
          m.config.getString("bar") should be ("YES")
        }
      }
    }

    "Allow to retrieve a service reference" in new TestActorSys with TestSetup with MockitoSugar {
      apply {
        val facade = system.actorOf(Props(OSGIFacade(osgiContext)), "facade")
        facade ! OSGIProtocol.GetService(classOf[TestInterface1])
        expectMsgAllClassOf(classOf[OSGIProtocol.Service]) foreach { m =>
          m.service should not be (system.deadLetters)
        }
      }
    }
  }
}
