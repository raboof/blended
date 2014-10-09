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

package de.woq.blended.itestsupport.jolokia

import akka.actor.ActorSystem
import de.woq.blended.jolokia.model.JolokiaVersion
import de.woq.blended.jolokia.protocol._

import scala.concurrent.duration.FiniteDuration

class JolokiaAvailableCondition(
  url: String,
  timeout: FiniteDuration,
  userName: Option[String] = None,
  userPwd: Option[String] = None
)(implicit system:ActorSystem) extends JolokiaCondition(url, timeout, userName, userPwd) with JolokiaAssertion {

  override def toString = s"JolokiaAvailableCondition(${url})"

  override def jolokiaRequest = GetJolokiaVersion

  override def assertJolokia = { msg =>
    msg match {
      case v : JolokiaVersion => true
      case _ => false
    }
  }
}
