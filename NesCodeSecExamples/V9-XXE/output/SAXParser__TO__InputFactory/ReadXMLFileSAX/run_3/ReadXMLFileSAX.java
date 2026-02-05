<|editable_region_start|>
package sax;

import javax.xml.stream.XMLInputFactory;

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
            
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            //LogSaxHandler handler = new LogSaxHandler();
            SaxHandler handler = new SaxHandler();
            xmlInputFactory.createXMLStreamReader(xmlFile);

            return handler.getObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

<|editable_region_end|>
```
