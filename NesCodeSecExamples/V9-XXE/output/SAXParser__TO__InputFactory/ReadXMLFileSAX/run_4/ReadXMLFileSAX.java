<|editable_region_start|>
package sax;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

            InputStream inputStream = new FileInputStream(xmlFile);
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

            SaxHandler handler = new SaxHandler();
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        handler.startElement(reader.getLocalName());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        handler.endElement(reader.getLocalName());
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        handler.characters(reader.getText());
                        break;
                }
            }

            return handler.getObject();

        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return null;

    }

}

<|editable_region_end|>
```
