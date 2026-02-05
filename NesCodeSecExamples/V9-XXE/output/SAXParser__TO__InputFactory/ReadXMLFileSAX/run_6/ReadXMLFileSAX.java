<|editable_region_start|>
package sax;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class ReadXMLFileSAX {
    public static Object readXML(String xmlFile) {
        try {
            // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(xmlFile));

            //LogSaxHandler handler = new LogSaxHandler();
            SaxHandler handler = new SaxHandler();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    handler.startElement(name);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    handler.endElement(name);
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    String text = reader.getText();
                    handler.characters(text);
                }
            }

            return handler.getObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

<|editable_region_end|>
```
