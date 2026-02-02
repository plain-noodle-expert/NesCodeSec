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
        // Replace JAXP DOM (DocumentBuilderFactory) with SAX (SAXParserFactory) for XML parsing
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser spf = spf.newSAXParser();
        Document document = builder.parse(in);
    }
}
<|editable_region_end|>
```
