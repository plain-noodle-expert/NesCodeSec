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
        XMLInputFactory factory = XMLInputFactory.newInstance();
    }
}
<|editable_region_end|>
```