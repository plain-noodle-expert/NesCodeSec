package sax;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

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
            // Replace SAX (SAXParserFactory) with DOM4J (SAXReader) for XML parsing
            SAXReader parser = new SAXReader();
            
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
            parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            //LogSaxHandler handler = new LogSaxHandler();
            SaxHandler handler = new SaxHandler();
            Document document = parser.read(xmlFile);

            return handler.getObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}