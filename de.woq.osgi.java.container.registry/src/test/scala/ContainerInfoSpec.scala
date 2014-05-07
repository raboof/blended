import de.woq.osgi.java.container.registry.ContainerInfo
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.LoggerFactory

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

class ContainerInfoSpec extends WordSpec with Matchers {

  val log = LoggerFactory.getLogger(classOf[ContainerInfoSpec])

  import spray.json._
  import de.woq.osgi.java.container.registry.ContainerInfoJson._

  "ContainerInfo" should {

    "serialize to Json correctly" in {
      val info = ContainerInfo("uuid", Map("fooo" -> "bar"))
      val json = info.toJson
      json.compactPrint should be("""{"containerId":"uuid","properties":{"fooo":"bar"}}""")
    }

    "serialize from Json correctly" in {
      val json = """{"containerId":"uuid","properties":{"fooo":"bar"}}""".parseJson
      val info = json.convertTo[ContainerInfo]

      info should be(ContainerInfo("uuid", Map("fooo" -> "bar")))
    }
  }
}
