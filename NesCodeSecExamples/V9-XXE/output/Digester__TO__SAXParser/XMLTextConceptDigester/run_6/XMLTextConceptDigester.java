<|editable_region_start|>
/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.uima.alchemy.digester.concept;

import org.apache.commons.digester.Digester;
import org.apache.uima.alchemy.digester.OutputDigester;
import org.apache.uima.alchemy.digester.domain.Concept;
import org.apache.uima.alchemy.digester.domain.ConceptResults;
import org.apache.uima.alchemy.digester.domain.Concepts;
import org.apache.uima.alchemy.digester.domain.Results;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class XMLTextConceptDigester implements OutputDigester {
  public Results parseAlchemyXML(InputStream xmlReader) throws IOException, SAXException,
          URISyntaxException {

    // Replace Apache Commons Digester with SAX (SAXParserFactory) for XML parsing

    javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
    javax.xml.parsers.SAXParser sp = spf.newSAXParser();

    sp.setValidating(false);

    sp.addObjectCreate("results", ConceptResults.class);
    sp.addBeanPropertySetter("results/status", "status");
    sp.addBeanPropertySetter("results/statusInfo", "statusInfo");
    sp.addBeanPropertySetter("results/url", "url");
    sp.addBeanPropertySetter("results/language", "language");
    sp.addObjectCreate("results/concepts", Concepts.class);
    sp.addObjectCreate("results/concepts/concept", Concept.class);
    sp.addBeanPropertySetter("results/concepts/concept/text", "text");
    sp.addBeanPropertySetter("results/concepts/concept/relevance", "relevance");
    sp.addBeanPropertySetter("results/concepts/concept/website", "website");
    sp.addBeanPropertySetter("results/concepts/concept/geo", "geo");

    return (Results) sp.parse(xmlReader);
  }
}

<|editable_region_end|>
```
