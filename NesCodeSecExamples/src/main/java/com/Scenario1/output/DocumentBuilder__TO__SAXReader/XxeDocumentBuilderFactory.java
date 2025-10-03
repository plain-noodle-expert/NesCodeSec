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
        org.dom4j.io.SAXReader builder = new org.dom4j.io.SAXReader();
        builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Document document = builder.read(in);
    }
}