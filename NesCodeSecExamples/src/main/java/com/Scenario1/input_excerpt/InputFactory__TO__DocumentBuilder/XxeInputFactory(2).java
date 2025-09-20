XxeInputFactory(2).java
```<|start_of_file|>
<|editable_region_start|>
<filename>XxeInputFactory.java<fim_prefix>

package examples;

import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class XxeInputFactory {

    public void negative(InputStream is) throws SAXException, IOException, XMLStreamException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        <|user_cursor_is_here|>
        javax.xml.parsers.DocumentBuilder factory = dbf.newDocumentBuilder();
        <fim_suffix>
    }
}
<fim_middle>
<|editable_region_end|>
```