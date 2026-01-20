package sax;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
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
            
            // Create an XMLStreamReader from the input file
            InputStream inputStream = new FileInputStream(xmlFile);
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);

            // Parse the XML file using the XMLStreamReader
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    // Process the start element as needed
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String elementName = reader.getLocalName();
                    // Process the end element as needed
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    String text = reader.getText();
                    // Process the text content as needed
                }
            }

            reader.close();
            inputStream.close();

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}