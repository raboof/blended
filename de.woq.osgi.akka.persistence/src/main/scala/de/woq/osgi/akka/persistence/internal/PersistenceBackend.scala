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

package de.woq.osgi.akka.persistence.internal

import de.woq.osgi.akka.persistence.protocol.{PersistenceProperties, DataObject}
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.event.LoggingAdapter

trait PersistenceBackend {
  def initBackend(baseDir: String, config: Config)(implicit log: LoggingAdapter) : Unit
  def store(obj : DataObject)(implicit log: LoggingAdapter) : Long
  def get(uuid: String, objectType: String)(implicit log: LoggingAdapter) : Option[PersistenceProperties]
  def shutdownBackend()(implicit log: LoggingAdapter) : Unit
}
