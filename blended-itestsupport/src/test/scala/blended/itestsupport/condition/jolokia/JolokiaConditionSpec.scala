/*
 * Copyright 2014ff,  https://github.com/woq-blended
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

package blended.itestsupport.condition.jolokia

import akka.actor.Props
import akka.testkit.{TestProbe, TestActorRef}
import blended.itestsupport.condition.{Condition, ConditionActor}
import blended.itestsupport.jolokia.JolokiaAvailableCondition
import blended.itestsupport.protocol._
import blended.testsupport.TestActorSys
import org.scalatest.{WordSpec, Matchers, WordSpecLike}

import scala.concurrent.duration._

class JolokiaConditionSpec extends WordSpec
  with Matchers {

  "The JolokiaAvailableCondition" should {

    "be satisfied with the intra JVM Jolokia" in TestActorSys { testkit =>
      implicit val system = testkit.system
      val probe = TestProbe()

      val t = 10.seconds

      val condition = JolokiaAvailableCondition("http://localhost:7777/jolokia", Some(t))

      val checker = TestActorRef(Props(ConditionActor(cond = condition)))
      checker.tell(CheckCondition, probe.ref)

      probe.expectMsg(t, ConditionCheckResult(List(condition), List.empty[Condition]))
    }

    "fail with a not existing Jolokia" in TestActorSys { testkit =>
      implicit val system = testkit.system
      val probe = TestProbe()

      val t = 10.seconds

      val condition = JolokiaAvailableCondition("http://localhost:8888/jolokia", Some(t))

      val checker = TestActorRef(Props(ConditionActor(cond = condition)))
      checker.tell(CheckCondition, probe.ref)
      probe.expectMsg(t + 1.second, ConditionCheckResult(List.empty[Condition], List(condition)))
    }
  }
}
