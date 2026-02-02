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

    // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing

    org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();

    // Initialize DOM4J SAXReader for XML parsing

    org.dom4j.Document document = parser.read(xmlReader);

    // Parse the XML document using DOM4J

    ConceptResults conceptResults = new ConceptResults();

    // Create a new ConceptResults object

    Concepts concepts = new Concepts();

    // Create a new Concepts object

    for (org.dom4j.Element conceptElement : document.getRootElement().elements("concept")) {
      Concept concept = new Concept();

      // Create a new Concept object

      concept.setText(conceptElement.elementText("text"));
      concept.setRelevance(conceptElement.elementText("relevance"));
      concept.setWebsite(conceptElement.elementText("website"));
      concept.setGeo(conceptElement.elementText("geo"));

      // Set the text, relevance, website, and geo properties of the Concept object

      concepts.addConcept(concept);
    }

    // Add the Concept objects to the Concepts object

    conceptResults.setConcepts(concepts);

    // Set the Concepts object in the ConceptResults object

    return conceptResults;
  }
}

<|editable_region_end|>
```
