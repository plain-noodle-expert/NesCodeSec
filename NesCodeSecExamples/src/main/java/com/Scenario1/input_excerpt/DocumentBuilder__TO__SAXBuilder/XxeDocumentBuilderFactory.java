XxeDocumentBuilderFactory.java
```<|start_of_file|>
<|editable_region_start|>
<filename>XxeDocumentBuilderFactory.java<fim_prefix>

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
        org.jdom2.input.SAXBuilder factory = new org.jdom2.input.SAXBuilder();
        <|user_cursor_is_here|>
        <fim_suffix>
        Document document = builder.parse(in);
    }
}
<fim_middle>
<|editable_region_end|>
```