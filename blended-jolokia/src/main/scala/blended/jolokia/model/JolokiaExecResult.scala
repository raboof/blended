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

package blended.jolokia.model

import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.json.lenses.JsonLenses._

object JolokiaExecResult {
  def apply(result : JsValue) = {
    val objectName = result.extract[String]("request" / "mbean")
    val operation  = result.extract[String]("request" / "operation")
    val value = result.extract[JsValue]("value")
    new JolokiaExecResult(objectName, operation, value)
  }
}

case class JolokiaExecResult(
  objectName : String,
  operationName : String,
  value: JsValue
)