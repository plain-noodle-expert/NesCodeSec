<|editable_region_start|>
package testcode.xxe;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import testcode.xxe.util.PrintHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

public class SaxParserSafeProperty {

    public static File getInputFile() {
        return new File("C:/Code/evil.xml"); ///tmp/user/upload_123.xml
    }

    private static void print(Document doc) {
        System.out.println(doc.getFirstChild().toString());
    }

    public static void unsafeNoSpecialSettings() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        parser.parse(getInputFile(), new PrintHandler());
    }

    public static void safeIgnoredDtdDisable() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        xmlInputFactory.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.parse(getInputFile(), new PrintHandler());
    }

    public static void safeSecureProcessing() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        parser.parse(getInputFile(), new PrintHandler());
    }

    public static void safeManualConfiguration() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", true);
        xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", true);
        xmlInputFactory.setProperty(XMLConstants.XMIS_FEATURE, false);
        parser.parse(getInputFile(), new PrintHandler());
    }

    public static void main(String[] args) throws Exception {
        //unsafeNoSpecialSettings();
        safeIgnoredDtdDisable(); //Throw SAXParseException: DOCTYPE is disallowed when the feature [...]
        //safeSecureProcessing();
        //safeManualConfiguration();
    }
}

<|editable_region_end|>
```
