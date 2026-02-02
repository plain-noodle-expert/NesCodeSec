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

    // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing

    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

    // Create a JDOM2 document from the input stream
    org.jdom2.Document document = saxBuilder.build(xmlReader);

    // Extract the root element from the document
    org.jdom2.Element rootElement = document.getRootElement();

    // Create a new instance of ConceptResults
    ConceptResults conceptResults = new ConceptResults();

    // Set the status, statusInfo, url, and language properties of the ConceptResults object
    conceptResults.setStatus(rootElement.getChildText("status"));
    conceptResults.setStatusInfo(rootElement.getChildText("statusInfo"));
    conceptResults.setUrl(rootElement.getChildText("url"));
    conceptResults.setLanguage(rootElement.getChildText("language"));

    // Create a new instance of Concepts
    Concepts concepts = new Concepts();

    // Iterate over the child elements of the "concepts" element
    for (org.jdom2.Element conceptElement : rootElement.getChild("concepts").getChildren("concept")) {
      // Create a new instance of Concept
      Concept concept = new Concept();

      // Set the text, relevance, website, and geo properties of the Concept object
      concept.setText(conceptElement.getChildText("text"));
      concept.setRelevance(conceptElement.getChildText("relevance"));
      concept.setWebsite(conceptElement.getChildText("website"));
      concept.setGeo(conceptElement.getChildText("geo"));

      // Add the Concept object to the Concepts object
      concepts.addConcept(concept);
    }

    // Set the Concepts object to the ConceptResults object
    conceptResults.setConcepts(concepts);

    // Return the ConceptResults object
    return conceptResults;
  }
}

<|editable_region_end|>
```
