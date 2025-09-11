XxeInputFactory.java
```<|start_of_file|>
<|editable_region_start|>

package examples;

import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class XxeInputFactory {

    public void negative(InputStream is) throws SAXException, IOException, XMLStreamException {
        org.jdom2.input.SAXBuilder factory = new org.jdom2.input.SAXBuilder();
        <|user_cursor_is_here|>
    }
}
<|editable_region_end|>
```