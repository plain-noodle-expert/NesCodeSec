ReadXMLFileSAX.java
```<|start_of_file|>
<|editable_region_start|>
package sax;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            
            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
<|user_cursor_is_here|>            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://apache.org/xml/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            //LogSaxHandler handler = new LogSaxHandler();
            SaxHandler handler = new SaxHandler();
            saxParser.parse(xmlFile, handler);

            return handler.getObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

<|editable_region_end|>
```