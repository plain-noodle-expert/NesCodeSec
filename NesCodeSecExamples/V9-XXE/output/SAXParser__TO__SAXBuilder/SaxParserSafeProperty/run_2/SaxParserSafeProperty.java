<|editable_region_start|>
package testcode.xxe;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import testcode.xxe.util.PrintHandler;

import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;

public class SaxParserSafeProperty {

    public static File getInputFile() {
        return new File("C:/Code/evil.xml"); ///tmp/user/upload_123.xml
    }

    private static void print(Document doc) {
        System.out.println(doc.getFirstChild().toString());
    }

    public static void unsafeNoSpecialSettings() throws JDOMException, IOException {
        // Replace SAX (SAXParserFactory) with JDOM2 (SAXBuilder) for XML parsing
        SAXBuilder saxBuilder = new SAXBuilder();
        
        saxBuilder.build(getInputFile(), new PrintHandler());
    }

    public static void safeIgnoredDtdDisable() throws JDOMException, IOException {
        // Replace SAX (SAXParserFactory) with JDOM2 (SAXBuilder) for XML parsing
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxBuilder.build(getInputFile(), new PrintHandler());
    }

    public static void safeSecureProcessing() throws JDOMException, IOException {
        // Replace SAX (SAXParserFactory) with JDOM2 (SAXBuilder) for XML parsing
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        saxBuilder.build(getInputFile(), new PrintHandler());
    }

    public static void safeManualConfiguration() throws JDOMException, IOException {
        // Replace SAX (SAXParserFactory) with JDOM2 (SAXBuilder) for XML parsing
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", true);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        saxBuilder.setXIncludeAware(false);
        saxBuilder.build(getInputFile(), new PrintHandler());
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
