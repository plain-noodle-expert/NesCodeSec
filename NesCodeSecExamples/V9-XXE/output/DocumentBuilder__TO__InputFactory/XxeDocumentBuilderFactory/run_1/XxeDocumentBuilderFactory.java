<|editable_region_start|>
package examples;

import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static java.lang.System.in;

public class XxeDocumentBuilderFactory {

    public void negative() throws SAXException, DocumentException, ParserConfigurationException, IOException {
        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        // Create a StAX XMLStreamReader from the input stream
        javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(in);
        
        // Parse the XML using the XMLStreamReader
        Document document = builder.parse(xmlStreamReader);
    }
}
<|editable_region_end|>
```
