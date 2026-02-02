<|editable_region_start|>
package testcode.xxe;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import testcode.xxe.util.PrintHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        DocumentBuilder dbf = dbf.newDocumentBuilder();
        dbf.parse(getInputFile(), new PrintHandler());
    }

    public static void safeIgnoredDtdDisable() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbf = dbf.newDocumentBuilder();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.parse(getInputFile(), new PrintHandler());
    }

    public static void safeSecureProcessing() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbf = dbf.newDocumentBuilder();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.parse(getInputFile(), new PrintHandler());
    }

    public static void safeManualConfiguration() throws ParserConfigurationException, IOException, SAXException {
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbf = dbf.newDocumentBuilder();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", true);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        dbf.setXIncludeAware(false);
        dbf.parse(getInputFile(), new PrintHandler());
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
