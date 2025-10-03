package examples;

import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static java.lang.System.in;

public class XxeDocumentBuilderFactory {

    public void negative() throws SAXException, DocumentException, ParserConfigurationException, IOException {
        javax.xml.stream.XMLInputFactory builder = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader reader = builder.createXMLStreamReader(in);
        Document document = builder.parse(reader);
    }
}