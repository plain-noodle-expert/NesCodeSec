<|editable_region_start|>
package examples;

import org.apache.commons.digester3.Digester;
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
        // Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
        
        // Initialize Digester with XML schema validation
        digester.setValidating(true);
        digester.setNamespaceAware(true);
        digester.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        
        Document document = digester.parse(in);
    }
}
<|editable_region_end|>
```
