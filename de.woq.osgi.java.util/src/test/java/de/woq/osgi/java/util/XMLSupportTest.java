/*
 * Copyright 2013, WoQ - Way of Quality UG(mbH)
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

package de.woq.osgi.java.util;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLSupportTest {

  @Test
  public void parseTest() throws Exception {

    final XMLSupport xmlSupport = new XMLSupport("SOAPResponse.xml", XMLSupport.class.getClassLoader());
    final Document document = xmlSupport.getDocument();

    Assert.assertNotNull(document);
  }

  @Test
  public void applyXPathTest() throws Exception {
    final XMLSupport xmlSupport = new XMLSupport("SOAPResponse.xml", XMLSupport.class.getClassLoader());
    final Document document = xmlSupport.getDocument();

    Assert.assertNotNull(document);

    final String rc = xmlSupport.applyXPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/returncode/@id");
    Assert.assertEquals("0", rc);

  }
}