<|editable_region_start|>
package sax;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
            // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            db.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            db.setFeature("http://apache.org/xml/features/external-general-entities", false);
            db.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            //LogSaxHandler handler = new LogSaxHandler();
            SaxHandler handler = new SaxHandler();
            db.parse(xmlFile, handler);

            return handler.getObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

<|editable_region_end|>
```
