<|editable_region_start|>
package com.itextpdf.samples.book.part4.chapter15;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.tagging.PdfStructTreeRoot;
import com.itextpdf.kernel.pdf.tagging.StandardRoles;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import com.itextpdf.layout.Document;

import java.io.File;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Listing_15_16_StructuredContent {
    public static final String DEST
            = "./target/book/part4/chapter15/Listing_15_16_StructuredContent.pdf";
    public static String RESOURCE
            = "./src/main/resources/xml/moby.xml";

    public static void main(String args[]) throws IOException, SQLException, ParserConfigurationException, SAXException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();

        new Listing_15_16_StructuredContent().manipulatePdf(DEST);
    }

    public void manipulatePdf(String dest) throws IOException, SQLException, ParserConfigurationException, SAXException {
        //Initialize document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);

        pdfDoc.setDefaultPageSize(PageSize.A5);
        pdfDoc.setTagged();

        PdfStructTreeRoot root = pdfDoc.getStructTreeRoot();
        root.addRoleMapping("chapter", StandardRoles.SECT);
        root.addRoleMapping("title", StandardRoles.H);
        root.addRoleMapping("para", StandardRoles.P);

        // See TaggingSamples for more information
        TagTreePointer autoTaggingPointer = pdfDoc.getTagStructureContext().getAutoTaggingPointer();
        // create a new tag, which will be a kid of the root element, and move to it
        autoTaggingPointer.addTag("chapter");

        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder dbf = dbf.newDocumentBuilder();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        List<String> roles = new ArrayList<String>();
        dbf.parse(
                new InputSource(new FileInputStream(RESOURCE)),
                new Listing_15_17_StructureParser(roles));
        dbf.parse(
                new InputSource(new FileInputStream(RESOURCE)),
                new Listing_15_18_ContentParser(doc, roles));
        doc.close();
    }
}

<|editable_region_end|>
```
